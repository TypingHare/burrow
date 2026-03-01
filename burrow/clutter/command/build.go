package command

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func BuildCommand(
	chamber *kernel.Chamber,
	clutterDecoration share.ClutterDecorationLike,
) *cobra.Command {
	var minimal bool

	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		RunE: func(cmd *cobra.Command, args []string) error {
			burrowSourceDir := filepath.Join(
				chamber.Burrow().GetSourceDir(),
				kernel.CartonName,
			)

			_, err := os.Stat(burrowSourceDir)
			if errors.Is(err, os.ErrNotExist) {
				return chamber.Error(
					fmt.Sprintf(
						"Burrow source directory does not exist: %s",
						burrowSourceDir,
					),
					nil,
				)
			} else if err != nil {
				return chamber.Error(
					fmt.Sprintf(
						"Failed to access Burrow source directory: %s",
						burrowSourceDir,
					),
					err,
				)
			}

			cartonNames := clutterDecoration.Spec().CartonNames
			localCartons := clutterDecoration.Spec().LocalCartons

			if minimal {
				return share.BuildMinimalBurrow(
					burrowSourceDir,
					chamber.Burrow().Env.Get(kernel.EnvMinimalExecutablePath),
				)
			} else {
				return share.BuildBurrow(
					burrowSourceDir,
					cartonNames,
					localCartons,
					chamber.Burrow().Env.Get(kernel.EnvExecutablePath),
				)
			}
		},
	}

	command.Flags().BoolVarP(&minimal, "minimal", "m", false,
		"Build a minimal version of Burrow without cartons.",
	)

	return command
}
