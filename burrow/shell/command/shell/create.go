package shell

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func CreateCommand(
	chamber *kernel.Chamber,
	shellDecoration share.ShellDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "create",
		Short: "Create a shell script",
		RunE: func(cmd *cobra.Command, args []string) error {
			filePath := share.GetShellFilePath(chamber.Burrow(), chamber.Name())
			content := share.GetShellFileContent(shellDecoration)

			err := os.WriteFile(filePath, []byte(content), 0o644)
			if err != nil {
				return fmt.Errorf("failed to create shell file: %w", err)
			}

			return nil
		},
	}

	return command
}
