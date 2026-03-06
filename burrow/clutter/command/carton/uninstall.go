package carton

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

// UninstallCommand builds the `carton uninstall` command.
func UninstallCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "uninstall <carton>",
		Short: "Uninstall a carton",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			err := share.UninstallCarton(
				d.Chamber(), d.Spec(), args[0],
			)
			if err != nil {
				return fmt.Errorf("failed to uninstall carton: %w", err)
			}

			return nil
		},
	}

	return command
}
