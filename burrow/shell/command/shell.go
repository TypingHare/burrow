package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/command/shell"
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ShellCommand(
	chamber *kernel.Chamber,
	shellDecoration share.ShellDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "shell",
		Short: "",
		RunE: func(cmd *cobra.Command, args []string) error {
			return nil
		},
	}

	command.AddCommand(shell.PathCommand(chamber, shellDecoration))
	command.AddCommand(shell.ContentCommand(chamber, shellDecoration))
	command.AddCommand(shell.CreateCommand(chamber, shellDecoration))
	command.AddCommand(shell.DeleteCommand(chamber, shellDecoration))

	return command
}
