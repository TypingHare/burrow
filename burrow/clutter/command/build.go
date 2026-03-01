package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func BuildCommand(
	chamber *kernel.Chamber,
	clutterDecoration share.ClutterDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonNames := clutterDecoration.Spec().CartonNames
			localCartonNames := clutterDecoration.Spec().LocalCartonNames
			return share.BuildBurrow(cartonNames, localCartonNames)
		},
	}

	return command
}
