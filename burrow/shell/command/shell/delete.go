package shell

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func DeleteCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "delete",
		Short: "Delete a shell script",
		RunE: func(cmd *cobra.Command, args []string) error {
			fileName := d.Spec().FileName
			if fileName == "" {
				fileName = d.Chamber().Name()
			}

			shellFilePath := share.GetShellFilePath(
				d.Chamber().Burrow(),
				fileName,
			)
			if err := os.Remove(shellFilePath); err != nil {
				return fmt.Errorf("Failed to delete the shell script: %w", err)
			}

			spec := d.Spec()
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
			if err := d.Chamber().UpdateAndSaveBlueprint(); err != nil {
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
