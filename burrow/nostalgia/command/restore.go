package command

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/spf13/cobra"
)

func RestoreCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "restore <datetime>",
		Short: "Restore a timestamped backup into the current chamber",
		Long: strings.TrimSpace(`
This command restores one saved backup into the current chamber.

Pass the timestamp string shown by "nostalgia list". Burrow uses that
timestamp to find the saved backup data and restore it into the chamber.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.Restore(d.Chamber(), args[0]); err != nil {
				return fmt.Errorf("failed to restore chamber backup: %w", err)
			}

			return nil
		},
	}

	return command
}
