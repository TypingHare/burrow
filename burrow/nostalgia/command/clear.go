package command

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// ClearCommand builds the `nostalgia clear` command.
func ClearCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "clear [datetime]",
		Short: "Remove one backup or all backups for the current chamber",
		Args:  cobra.MaximumNArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if len(args) == 1 {
				return clearBackup(d.Chamber(), args[0])
			}

			datetimeStrings, err := listBackupDatetimeStrings(d.Chamber())
			if err != nil {
				return fmt.Errorf("failed to list chamber backups: %w", err)
			}

			for _, datetimeString := range datetimeStrings {
				if err := clearBackup(d.Chamber(), datetimeString); err != nil {
					return err
				}
			}

			return nil
		},
	}

	return command
}

// clearBackup removes the timestamped nostalgia backup directories for a
// chamber.
func clearBackup(chamber *kernel.Chamber, datetimeString string) error {
	if _, err := share.GetDatetimeFromString(datetimeString); err != nil {
		return fmt.Errorf(
			"invalid backup datetime string %q: %w",
			datetimeString,
			err,
		)
	}

	for _, backupDir := range []string{
		share.GetConfigBackupDir(chamber, datetimeString),
		share.GetDataBackupDir(chamber, datetimeString),
	} {
		if err := os.RemoveAll(backupDir); err != nil {
			return fmt.Errorf(
				"failed to remove backup directory %q: %w",
				backupDir,
				err,
			)
		}
	}

	return nil
}
