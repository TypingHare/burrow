package shell

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func DeleteCommand(
	chamber *kernel.Chamber,
	shellDecoration share.ShellDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "delete",
		Short: "Delete a shell script",
		RunE: func(cmd *cobra.Command, args []string) error {
			fileName := shellDecoration.Spec().FileName
			if fileName == "" {
				fileName = chamber.Name()
			}

			shellFilePath := share.GetShellFilePath(chamber.Burrow(), fileName)
			if err := os.Remove(shellFilePath); err != nil {
				return fmt.Errorf("Failed to delete the shell script: %w", err)
			}

			spec := shellDecoration.Spec()
			newCreatedFileNames := make([]string, 0, len(spec.CreatedFileNames))
			for _, createdFileName := range spec.CreatedFileNames {
				if createdFileName != fileName {
					newCreatedFileNames = append(
						newCreatedFileNames,
						createdFileName,
					)
				}
			}
			spec.CreatedFileNames = newCreatedFileNames
			if err := chamber.UpdateAndSaveBlueprint(); err != nil {
				return fmt.Errorf(
					"failed to save shell decoration spec: %w",
					err,
				)
			}

			return nil
		},
	}

	return command
}
