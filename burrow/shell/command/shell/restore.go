package shell

import (
	"errors"
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/spf13/cobra"
)

func RestoreCommand(d share.ShellDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "restore",
		Short: "Restore missing shell scripts from createdFileNames",
		RunE: func(cmd *cobra.Command, args []string) error {
			content := share.GetShellFileContent(d)
			for _, fileName := range d.Spec().CreatedFileNames {
				if fileName == "" {
					continue
				}

				shellFilePath := share.GetShellFilePath(
					d.Chamber().Burrow(),
					fileName,
				)
				_, err := os.Stat(shellFilePath)
				if err == nil {
					continue
				}
				if !errors.Is(err, os.ErrNotExist) {
					return fmt.Errorf(
						"failed to check shell file %q: %w",
						shellFilePath,
						err,
					)
				}

				if err := os.WriteFile(
					shellFilePath,
					[]byte(content),
					0o644,
				); err != nil {
					return fmt.Errorf(
						"failed to restore shell file %q: %w",
						shellFilePath,
						err,
					)
				}
			}

			return nil
		},
	}

	return command
}
