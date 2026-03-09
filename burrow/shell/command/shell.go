package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/shell/command/shell"
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

// ShellCommand builds the `shell` command group for chamber launcher scripts.
func ShellCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "shell",
		Short: "Manage chamber specific shell files.",
		Long: strings.TrimSpace(`
This command group manages shell script files for the current chamber.

Use these commands to see the script path, print the generated script
content, create a script file, delete a created script, or restore missing
scripts from the shell decoration spec.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			return nil
		},
	}

	command.AddCommand(shell.PathCommand(d))
	command.AddCommand(shell.ContentCommand(d))
	command.AddCommand(shell.CreateCommand(d))
	command.AddCommand(shell.DeleteCommand(d))
	command.AddCommand(shell.RestoreCommand(d))

	return command
}
