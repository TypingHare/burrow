package share

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetRedirectorHandler returns a chamber handler that retries failed command
// execution with redirected arguments.
func GetRedirectorHandler(
	decor IDecor,
	coreDecor share.IDecor,
) func(*kernel.Chamber, []string) (int, error) {
	return func(chamber *kernel.Chamber, args []string) (int, error) {
		if chamber == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("chamber is nil")
		}

		rootCommand := coreDecor.RootCommand()
		if rootCommand == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("root command is nil")
		}

		rootCommand.SetArgs(args)
		err := rootCommand.Execute()
		if err == nil {
			return kernel.Success, nil
		}

		redirectorFunc := decor.Redirector()
		if redirectorFunc == nil {
			return kernel.GeneralError, chamber.Error(
				"error executing the command",
				err,
			)
		}

		newArgs, err := redirectorFunc(args)
		if err != nil {
			return kernel.GeneralError, fmt.Errorf(
				"failed to redirect: %w",
				err,
			)
		}

		// If not silently redirecting, print a message to indicate
		// redirection is happening.
		if !decor.SilentlyRedirect() {
			fmt.Println("Redirecting command execution...")
		}

		// Set the new arguments and retry execution.
		rootCommand.SetArgs(newArgs)
		err = rootCommand.Execute()
		if err != nil {
			return kernel.GeneralError, fmt.Errorf(
				"failed to execute redirected command: %w",
				err,
			)
		} else {
			return kernel.Success, nil
		}
	}
}
