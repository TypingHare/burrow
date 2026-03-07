package command

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/spf13/cobra"
)

// BackupCommand builds the `nostalgia backup` command.
func BackupCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "backup",
		Short: "Create a timestamped backup for the current chamber",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.Backup(d.Chamber()); err != nil {
				return fmt.Errorf("failed to back up chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
