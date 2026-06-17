package main

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// DefaultName is the default name of the Burrow.
const DefaultName = "burrow"

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.InitEnv(DefaultName); err != nil {
		fmt.Fprintf(
			os.Stderr,
			"failed to initialize environment variables: %v",
			err,
		)
		os.Exit(kernel.GeneralError)
	}

	burrow.LoadProcessEnv()

	setEnv(burrow.Env)
	registerCartons(burrow.Warehouse)

	// Burrow handles the command-line arguments.
	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		kernel.PrintErrorStack(err)
	}

	// Destroy the Burrow and exit with the appropriate exit code.
	if err := burrow.Destroy(); err != nil {
		kernel.PrintErrorStack(err)
		os.Exit(kernel.GeneralError)
	}

	os.Exit(exitCode)
}
