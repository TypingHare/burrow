package shell

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func ContentCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "content",
		Short: "Show the content of the shell file",
		Long: strings.TrimSpace(`
This command prints the shell script content that the shell decoration
would write to a shell file.

Use it when you want to inspect the generated script without creating or
modifying any files on disk.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			cmd.Println(share.GetShellFileContent(d))
			return nil
		},
	}

	return command
}
