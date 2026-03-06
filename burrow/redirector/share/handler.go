package share

import (
	"fmt"

	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetRedirectorHandler returns a chamber handler that retries failed command
// execution with redirected arguments.
func GetRedirectorHandler(
	d RedirectorDecorationLike,
	coreDecoration coreShare.CoreDecorationLike,
) func(*kernel.Chamber, []string) (int, error) {
	return func(chamber *kernel.Chamber, args []string) (int, error) {
		if chamber == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("chamber is nil")
		}

		rootCommand := coreDecoration.GetRootCommand()
		if rootCommand == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("root command is nil")
		}

		rootCommand.SetArgs(args)
		err := rootCommand.Execute()
		if err == nil {
			return kernel.Success, nil
		}

		redirectorFunc := d.GetRedirector()
		if redirectorFunc != nil {
			newArgs, err := redirectorFunc(args)
			if err != nil {
				return kernel.GeneralError, fmt.Errorf(
					"failed to redirect: %w",
					err,
				)
			}

			silent := d.Spec().SilentlyRedirect
			if !silent {
				fmt.Println("Redirecting command execution...")
			}

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

		return kernel.GeneralError, chamber.Error(
			"error executing the command",
			err,
		)
	}
}
