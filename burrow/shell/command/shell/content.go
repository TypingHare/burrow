package shell

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ContentCommand(
	chamber *kernel.Chamber,
	shellDecoration share.ShellDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "content",
		Short: "Display the content of the shell file",
		RunE: func(cmd *cobra.Command, args []string) error {
			cmd.Println(share.GetShellFileContent(shellDecoration))
			return nil
		},
	}

	return command
}
