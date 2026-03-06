package share

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetCoreHandler returns the chamber handler backed by the core decoration's
// root Cobra command.
func GetCoreHandler(
	d CoreDecorationLike,
) func(*kernel.Chamber, []string) (int, error) {
	return func(chamber *kernel.Chamber, args []string) (int, error) {
		if chamber == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("chamber is nil")
		}

		rootCommand := d.GetRootCommand()
		if rootCommand == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("root command is nil")
		}

		rootCommand.SetArgs(args)
		if err := rootCommand.Execute(); err != nil {
			return kernel.GeneralError, chamber.Error(
				"error executing the command",
				err,
			)
		}

		return kernel.Success, nil
	}
}
