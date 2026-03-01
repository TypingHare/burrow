package carton

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func UninstallCommand(
	chamber *kernel.Chamber,
	clutterDecoration share.ClutterDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "uninstall",
		Short: "Uninstall a carton",
		RunE: func(cmd *cobra.Command, args []string) error {
			return nil
		},
	}

	return command
}
