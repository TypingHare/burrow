package burrow

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func BuildCommand(d share.ClutterDecorationLike) *cobra.Command {
	var minimal bool

	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, args []string) error {
			return service.BuildBurrow(d, minimal)
		},
	}

	command.Flags().BoolVarP(&minimal, "minimal", "m", false,
		"Build a minimal version of Burrow without cartons",
	)

	return command
}
