package shell

import (
	"fmt"
	"os"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func CreateCommand(
	chamber *kernel.Chamber,
	shellDecoration share.ShellDecorationLike,
) *cobra.Command {
	var flagFileName string

	command := &cobra.Command{
		Use:   "create",
		Short: "Create a shell script",
		Args:  cobra.MaximumNArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			fileName := flagFileName
			if len(args) > 0 {
				fileName = args[0]
			}
			if fileName == "" {
				fileName = shellDecoration.Spec().FileName
			}
			if fileName == "" {
				fileName = chamber.Name()
			}

			shellFilePath := share.GetShellFilePath(chamber.Burrow(), fileName)
			content := share.GetShellFileContent(shellDecoration)
			err := os.WriteFile(shellFilePath, []byte(content), 0o644)
			if err != nil {
				return fmt.Errorf("failed to create shell file: %w", err)
			}

			spec := shellDecoration.Spec()
			if !slices.Contains(spec.CreatedFileNames, fileName) {
				spec.CreatedFileNames = append(spec.CreatedFileNames, fileName)
			}
			if err := chamber.UpdateAndSaveBlueprint(); err != nil {
				return fmt.Errorf(
					"failed to save shell decoration spec: %w",
					err,
				)
			}

			return nil
		},
	}

	command.Flags().
		StringVarP(&flagFileName, "filename", "f", "",
			"The name of the shell script file",
		)

	return command
}
