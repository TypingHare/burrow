package shell

import (
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func PathCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "path",
		Short: "Display the path to the shell script file",
		RunE: func(cmd *cobra.Command, args []string) error {
			cmd.Println(
				share.GetShellFilePath(chamber.Burrow(), chamber.Name()),
			)

			return nil
		},
	}

	return command
}
