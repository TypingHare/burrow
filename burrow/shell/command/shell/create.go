package shell

import (
	"fmt"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func CreateCommand(d share.ShellDecorationLike) *cobra.Command {
	var flagFileName string

	command := &cobra.Command{
		Use:   "create",
		Short: "Create a shell script",
		Long: strings.TrimSpace(`
This command creates a shell script file for the current chamber.

If you pass a file name argument or "--filename", Burrow uses that name.
Otherwise it uses the file name stored in the shell decoration spec, and
if that is empty it falls back to the chamber name.

After the file is created, the shell decoration spec remembers it in
"createdFileNames" so it can be restored later if needed.
		`),
		Args: cobra.MaximumNArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			fileName := flagFileName
			if len(args) > 0 {
				fileName = args[0]
			}
			if fileName == "" {
				fileName = d.Spec().FileName
			}
			if fileName == "" {
				fileName = d.Chamber().Name()
			}

			fileRelPath, err := share.CreateShellFile(d, fileName)
			if err != nil {
				return fmt.Errorf("failed to create shell file: %w", err)
			}

			// Update the shell decoration spec to include the created file name
			// if it's not already present.
			spec := d.Spec()
			if !slices.Contains(spec.CreatedFileNames, fileRelPath) {
				spec.CreatedFileNames = append(
					spec.CreatedFileNames,
					fileRelPath,
				)

				if err := d.Chamber().UpdateAndSaveBlueprint(); err != nil {
					return fmt.Errorf(
						"failed to save shell decoration spec: %w",
						err,
					)
				}
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
