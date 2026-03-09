package command

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/spf13/cobra"
)

func ListCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "list",
		Short: "List available backup timestamps for the current chamber",
		Long: strings.TrimSpace(`
This command lists the backup timestamps available for the current
chamber.

Each line in the output is a timestamp string that can be used with
"nostalgia restore" or "nostalgia clear".
		`),
		Args: cobra.NoArgs,
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
