package share

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// shouldRedirect reports whether args should be handed to the redirector.
//
// Cobra does not expose a typed "command not found" error, so resolving via
// Find is the reliable way to distinguish an unknown command from a real
// command that failed. Redirection happens only when the args contain a
// positional argument that does not resolve to a subcommand (e.g. a record
// name or URL). Flag-only invocations such as "--help" or "--version" resolve
// to the root command and must be executed normally, not redirected.
func shouldRedirect(root *cobra.Command, args []string) bool {
	cmd, remaining, err := root.Find(args)
	if err != nil {
		return true
	}

	// Resolved to an actual subcommand: let it execute.
	if cmd != root {
		return false
	}

	// Only the root matched. Redirect when there is an unresolved positional
	// argument; otherwise (no args, or flags only) let the root command run.
	for _, arg := range remaining {
		if !strings.HasPrefix(arg, "-") {
			return true
		}
	}

	return false
}

// GetRedirectorHandler returns a chamber handler that redirects unknown
// commands through the configured redirector, while letting real commands
// execute and surface their own errors.
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

		redirectorFunc := decor.Redirector()

		// Execute directly unless the args are an unknown command that we can
		// redirect. This ensures real commands surface their own errors instead
		// of being masked by the redirector.
		if redirectorFunc == nil || !shouldRedirect(rootCommand, args) {
			rootCommand.SetArgs(args)
			if err := rootCommand.Execute(); err != nil {
				return kernel.GeneralError, chamber.Error(
					"error executing the command",
					err,
				)
			}

			return kernel.Success, nil
		}

		newArgs, err := redirectorFunc(args)
		if err != nil {
			return kernel.GeneralError, fmt.Errorf(
				"failed to redirect: %w",
				err,
			)
		}

		// If the redirector returns an empty argument list, there is nothing to
		// execute.
		if len(newArgs) == 0 {
			return kernel.GeneralError, chamber.Error(
				"redirector produced no arguments",
				nil,
			)
		}

		// If not silently redirecting, print a message to indicate redirection
		// is happening.
		if !decor.SilentlyRedirect() {
			fmt.Println("Redirecting command execution...")
		}

		// Set the new arguments and retry execution.
		rootCommand.SetArgs(newArgs)
		if err := rootCommand.Execute(); err != nil {
			return kernel.GeneralError, fmt.Errorf(
				"failed to execute redirected command: %w",
				err,
			)
		}

		return kernel.Success, nil
	}
}
