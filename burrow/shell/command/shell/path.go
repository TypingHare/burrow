package shell

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func PathCommand(
	chamber *kernel.Chamber,
	shellDecoration share.ShellDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "path",
		Short: "Display the path to the shell script file",
		RunE: func(cmd *cobra.Command, args []string) error {
			fileName := shellDecoration.Spec().FileName
			if fileName == "" {
				fileName = chamber.Name()
			}

			shellFilePath := share.GetShellFilePath(chamber.Burrow(), fileName)
			cmd.Println(shellFilePath)

			return nil
		},
	}

	return command
}
