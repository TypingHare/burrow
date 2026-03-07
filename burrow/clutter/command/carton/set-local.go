package carton

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

// SetLocalCommand builds the `carton set-local` command.
func SetLocalCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "set-local <carton> <path>",
		Short: "Set a local path for an installed carton",
		Args:  cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			err := share.SetLocalCartonPath(
				d.Chamber(), d.Spec(), args[0], args[1],
			)
			if err != nil {
				return fmt.Errorf("failed to set local carton path: %w", err)
			}

			return nil
		},
	}

	return command
}
