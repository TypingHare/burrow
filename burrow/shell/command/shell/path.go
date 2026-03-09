package shell

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

// PathCommand builds the `shell path` command.
func PathCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "path",
		Short: "Show the path to the shell script file",
		Long: strings.TrimSpace(`
This command prints the full path to the chamber's shell script file.

Use it when you want to know where Burrow expects the generated launcher
script to live on disk.
		`),
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
