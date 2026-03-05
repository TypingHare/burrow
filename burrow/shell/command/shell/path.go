package shell

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func PathCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "path",
		Short: "Display the path to the shell script file",
		RunE: func(cmd *cobra.Command, args []string) error {
			fileName := d.Spec().FileName
			if fileName == "" {
				fileName = d.Chamber().Name()
			}

			shellFilePath := share.GetShellFilePath(
				d.Chamber().Burrow(),
				fileName,
			)
			cmd.Println(shellFilePath)

			return nil
		},
	}

	return command
}
