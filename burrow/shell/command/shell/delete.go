package shell

import (
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func DeleteCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "delete",
		Short: "Delete a shell script",
		RunE: func(cmd *cobra.Command, args []string) error {
			shellFilePath := share.GetShellFilePath(
				chamber.Burrow(),
				chamber.Name(),
			)

			if err := os.Remove(shellFilePath); err != nil {
				return chamber.Error(
					"Failed to delete the shell script: %v",
					err,
				)
			}

			return nil
		},
	}

	return command
}
