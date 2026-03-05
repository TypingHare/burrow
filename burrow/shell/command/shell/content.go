package shell

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func ContentCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "content",
		Short: "Show the content of the shell file",
		RunE: func(cmd *cobra.Command, args []string) error {
			cmd.Println(share.GetShellFileContent(d))
			return nil
		},
	}

	return command
}
