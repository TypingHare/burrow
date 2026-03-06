package burrow

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// BuildCommand builds the `burrow build` command.
func BuildCommand(d share.ClutterDecorationLike) *cobra.Command {
	var minimal bool

	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		RunE: func(cmd *cobra.Command, args []string) error {
			burrow := d.Chamber().Burrow()
			err := share.EnsureSourceDir(burrow, kernel.CartonName)
			if err != nil {
				return fmt.Errorf(
					"Failed to ensure Burrow source directory: %w",
					err,
				)
			}

			if minimal {
				return share.BuildMinimalBurrow(burrow)
			} else {
				return share.BuildBurrow(
					burrow,
					d.Spec().CartonNames,
					d.Spec().LocalCartons,
					d.Spec().MagicEnv,
				)
			}
		},
	}

	command.Flags().BoolVarP(&minimal, "minimal", "m", false,
		"Build a minimal version of Burrow without cartons",
	)

	return command
}
