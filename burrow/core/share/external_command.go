package share

import (
	"bytes"
	"fmt"
	"os/exec"
)

func RunExternalCommand(
	dir string,
	args []string,
) (string, string, int, error) {
	if len(args) == 0 {
		return "", "", 0, fmt.Errorf("no command provided")
	}

	cmd := exec.Command(args[0], args[1:]...)
	cmd.Dir = dir

	var stdout bytes.Buffer
	var stderr bytes.Buffer

	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	err := cmd.Run()
	exitCode := 0
	if err != nil {
		if exitErr, ok := err.(*exec.ExitError); ok {
			exitCode = exitErr.ExitCode()
		}
	}

	return stdout.String(), stderr.String(), exitCode, err
}
