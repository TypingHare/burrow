package command

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/spf13/cobra"
)

// ListCommand builds the `nostalgia list` command.
func ListCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "list",
		Short: "List available backup timestamps for the current chamber",
		Args:  cobra.NoArgs,
		RunE: func(cmd *cobra.Command, args []string) error {
			datetimeStrings, err := listBackupDatetimeStrings(d.Chamber())
			if err != nil {
				return fmt.Errorf("failed to list chamber backups: %w", err)
			}

			for _, datetimeString := range datetimeStrings {
				cmd.Println(datetimeString)
			}

			return nil
		},
	}

	return command
}
