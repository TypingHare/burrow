package burrow

import (
	"fmt"

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
			err := share.EnsureSourceDir(chamber.Burrow(), kernel.CartonName)
			if err != nil {
				return fmt.Errorf(
					"Failed to ensure Burrow source directory: %w",
					err,
				)
			}

			if minimal {
				return share.BuildMinimalBurrow(chamber.Burrow())
			} else {
				return share.BuildBurrow(
					chamber.Burrow(),
					clutterDecoration.Spec().CartonNames,
					clutterDecoration.Spec().LocalCartons,
					clutterDecoration.Spec().MagicEnv,
				)
			}
		},
	}

	command.Flags().BoolVarP(&minimal, "minimal", "m", false,
		"Build a minimal version of Burrow without cartons",
	)

	return command
}
