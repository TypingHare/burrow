package core

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// CoreHandler is the main handler for the core chamber. It executes the root
// command with the provided arguments.
func CoreHandler(chamber *kernel.Chamber, args []string) (int, error) {
	if chamber == nil {
		return kernel.ErrorNullPointer, fmt.Errorf("chamber is nil")
	}

	coreDecoration, err := kernel.Use[*CoreDecoration](chamber)
	if err != nil {
		return kernel.GeneralError, fmt.Errorf(
			"failed to use core decoration: %w",
			err,
		)
	}

	rootCommand := coreDecoration.RootCommand
	if rootCommand == nil {
		return kernel.ErrorNullPointer, fmt.Errorf("root command is nil")
	}

	rootCommand.SetArgs(args)
	if err := rootCommand.Execute(); err != nil {
		return kernel.GeneralError, err
	}

	return kernel.Success, nil
}
