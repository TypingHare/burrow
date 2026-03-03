package carton

import (
	"fmt"

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
			err := share.UninstallCarton(
				chamber,
				clutterDecoration.Spec(),
				args[0],
			)
			if err != nil {
				return fmt.Errorf("failed to uninstall carton: %w", err)
			}

			return nil
		},
	}

	return command
}
