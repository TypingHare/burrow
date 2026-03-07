package command

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/spf13/cobra"
)

// RestoreCommand builds the `nostalgia restore` command.
func RestoreCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "restore <datetime>",
		Short: "Restore a timestamped backup into the current chamber",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.Restore(d.Chamber(), args[0]); err != nil {
				return fmt.Errorf("failed to restore chamber backup: %w", err)
			}

			return nil
		},
	}

	return command
}
