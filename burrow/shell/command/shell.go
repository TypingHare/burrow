package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/command/shell"
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func ShellCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "shell",
		Short: "Manage chamber specific shell files.",
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
