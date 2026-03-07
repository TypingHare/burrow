package carton

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

// UnsetLocalCommand builds the `carton unset-local` command.
func UnsetLocalCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "unset-local <carton>",
		Short: "Unset the local path for an installed carton",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			err := share.UnsetLocalCartonPath(
				d.Chamber(), d.Spec(), args[0],
			)
			if err != nil {
				return fmt.Errorf("failed to unset local carton path: %w", err)
			}

			return nil
		},
	}

	return command
}
