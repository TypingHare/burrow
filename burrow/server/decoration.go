package server

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net"
	"net/http"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/server/command"
	"github.com/TypingHare/burrow/v2026/burrow/server/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type ServerDecoration struct {
	kernel.Decoration[share.ServerSpec]

	burrow       *kernel.Burrow
	server       *http.Server
	listener     net.Listener
	address      string
	commandExecM sync.Mutex
}

func (d *ServerDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *ServerDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"listenAddress":   d.Spec().ListenAddress,
		"commandPath":     d.Spec().CommandPath,
		"maxBodyBytes":    d.Spec().MaxBodyBytes,
		"readTimeoutMS":   d.Spec().ReadTimeoutMS,
		"writeTimeoutMS":  d.Spec().WriteTimeoutMS,
		"idleTimeoutMS":   d.Spec().IdleTimeoutMS,
		"shutdownGraceMS": d.Spec().ShutdownGraceMS,
	}
}

func (d *ServerDecoration) Burrow() *kernel.Burrow {
	return d.burrow
}

func (d *ServerDecoration) Assemble() error {
	coreDecoration, err := core.UseDecoration(d)
	if err != nil {
		return fmt.Errorf("failed to use core decoration: %w", err)
	}

	if d.Spec().ListenAddress == "" {
		d.Spec().ListenAddress = ":8080"
	}

	if d.Spec().CommandPath == "" {
		d.Spec().CommandPath = "/"
	}

	if d.Spec().MaxBodyBytes <= 0 {
		d.Spec().MaxBodyBytes = 1 << 20
	}

	if d.Spec().ReadTimeoutMS <= 0 {
		d.Spec().ReadTimeoutMS = 5000
	}

	if d.Spec().WriteTimeoutMS <= 0 {
		d.Spec().WriteTimeoutMS = 5000
	}

	if d.Spec().IdleTimeoutMS <= 0 {
		d.Spec().IdleTimeoutMS = 60000
	}

	if d.Spec().ShutdownGraceMS <= 0 {
		d.Spec().ShutdownGraceMS = 5000
	}

	if err := coreDecoration.SetCommand(
		nil,
		command.ServerCommand(d),
	); err != nil {
		return fmt.Errorf("failed to set server commands: %w", err)
	}

	return nil
}

func (d *ServerDecoration) Launch() error { return nil }

func (d *ServerDecoration) Terminate() error {
	return nil
}

func (d *ServerDecoration) Disassemble() error { return nil }

func (d *ServerDecoration) Start() error {
	listener, err := net.Listen("tcp", d.Spec().ListenAddress)
	if err != nil {
		return fmt.Errorf(
			"failed to listen on %q: %w",
			d.Spec().ListenAddress,
			err,
		)
	}

	server := &http.Server{
		Addr:         d.Spec().ListenAddress,
		Handler:      http.HandlerFunc(d.handleRequest),
		ReadTimeout:  time.Duration(d.Spec().ReadTimeoutMS) * time.Millisecond,
		WriteTimeout: time.Duration(d.Spec().WriteTimeoutMS) * time.Millisecond,
		IdleTimeout:  time.Duration(d.Spec().IdleTimeoutMS) * time.Millisecond,
	}

	d.listener = listener
	d.server = server
	d.address = listener.Addr().String()

	fmt.Printf("listening on %s%s\n", d.address, d.Spec().CommandPath)

	serverErrChan := make(chan error, 1)
	go func() {
		serverErrChan <- server.Serve(listener)
	}()

	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)
	defer signal.Stop(signalChan)

	select {
	case sig := <-signalChan:
		fmt.Printf("shutting down after %s\n", sig)
	case err := <-serverErrChan:
		if err == nil || err == http.ErrServerClosed {
			return nil
		}
		return fmt.Errorf("server stopped unexpectedly: %w", err)
	}

	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Duration(d.Spec().ShutdownGraceMS)*time.Millisecond,
	)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		return fmt.Errorf("failed to shut down server: %w", err)
	}

	err = <-serverErrChan
	if err != nil && err != http.ErrServerClosed {
		return fmt.Errorf("server stopped unexpectedly: %w", err)
	}

	d.server = nil
	d.listener = nil
	d.address = ""

	return nil
}

func (d *ServerDecoration) handleRequest(
	writer http.ResponseWriter,
	request *http.Request,
) {
	if request.URL.Path != d.Spec().CommandPath {
		http.NotFound(writer, request)
		return
	}

	if request.Method != http.MethodPost {
		writer.Header().Set("Allow", http.MethodPost)
		http.Error(writer, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	request.Body = http.MaxBytesReader(
		writer,
		request.Body,
		d.Spec().MaxBodyBytes,
	)

	var args []string
	decoder := json.NewDecoder(request.Body)
	if err := decoder.Decode(&args); err != nil {
		http.Error(writer, "invalid request body", http.StatusBadRequest)
		return
	}

	var trailing json.RawMessage
	if err := decoder.Decode(&trailing); err == nil {
		http.Error(
			writer,
			"request body must contain a single JSON value",
			http.StatusBadRequest,
		)
		return
	}

	stdout, stderr, exitCode := d.executeArgs(args)

	writer.Header().Set("Content-Type", "application/json")
	if exitCode != kernel.Success {
		writer.WriteHeader(http.StatusBadRequest)
	}

	response := struct {
		Stdout   string `json:"stdout"`
		Stderr   string `json:"stderr"`
		ExitCode int    `json:"exitCode"`
	}{
		Stdout:   stdout,
		Stderr:   stderr,
		ExitCode: exitCode,
	}

	if err := json.NewEncoder(writer).Encode(response); err != nil {
		http.Error(
			writer,
			"failed to encode response",
			http.StatusInternalServerError,
		)
	}
}

func (d *ServerDecoration) executeArgs(args []string) (string, string, int) {
	coreDecoration, err := core.UseDecoration(d)
	if err != nil {
		return "", err.Error(), kernel.GeneralError
	}

	rootCommand := coreDecoration.GetRootCommand()
	if rootCommand == nil {
		return "", "root command is nil", kernel.ErrorNullPointer
	}

	d.commandExecM.Lock()
	defer d.commandExecM.Unlock()

	var stdout bytes.Buffer
	var stderr bytes.Buffer

	rootCommand.SetOut(&stdout)
	rootCommand.SetErr(&stderr)

	exitCode, execErr := d.Chamber().Handler(d.Chamber(), args)
	if execErr != nil && stderr.Len() == 0 {
		stderr.WriteString(execErr.Error())
	}

	rootCommand.SetOut(os.Stdout)
	rootCommand.SetErr(os.Stderr)

	return stdout.String(), stderr.String(), exitCode
}

func BuildServerDecoration(
	chamber *kernel.Chamber,
	spec *share.ServerSpec,
) (kernel.DecorationInstance, error) {
	return &ServerDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
		burrow:     chamber.Burrow().Clone(),
	}, nil
}

func UseDecoration(
	d kernel.DecorationInstance,
) (*ServerDecoration, error) {
	return kernel.Use[*ServerDecoration](d.Chamber())
}
