package command

import (
	"fmt"
	"os"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// NostalgiaCommand builds the `nostalgia` command group for chamber backups.
func NostalgiaCommand(d share.NostalgiaDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "nostalgia",
		Short: "Manage backups for the chamber",
		Long: strings.TrimSpace(`
This command group manages chamber backups created by the nostalgia
decoration.

Use these commands to create timestamped backups, list the backups that
exist, restore one backup into the current chamber, or delete old
backups.
		`),
	}

	command.AddCommand(BackupCommand(d))
	command.AddCommand(ListCommand(d))
	command.AddCommand(RestoreCommand(d))
	command.AddCommand(ClearCommand(d))

	return command
}

// listBackupDatetimeStrings returns sorted backup timestamps discovered in
// either nostalgia backup directory.
func listBackupDatetimeStrings(chamber *kernel.Chamber) ([]string, error) {
	datetimeSet := map[string]struct{}{}

	for _, backupRoot := range []string{
		share.GetNostalgiaConfigDir(chamber),
		share.GetNostalgiaDataDir(chamber),
	} {
		entries, err := os.ReadDir(backupRoot)
		if err != nil {
			if os.IsNotExist(err) {
				continue
			}
			return nil, fmt.Errorf(
				"failed to read backup directory %q: %w",
				backupRoot,
				err,
			)
		}

		for _, entry := range entries {
			if !entry.IsDir() {
				continue
			}
			if _, err := share.GetDatetimeFromString(entry.Name()); err != nil {
				continue
			}
			datetimeSet[entry.Name()] = struct{}{}
		}
	}

	datetimeStrings := make([]string, 0, len(datetimeSet))
	for datetimeString := range datetimeSet {
		datetimeStrings = append(datetimeStrings, datetimeString)
	}
	slices.Sort(datetimeStrings)

	return datetimeStrings, nil
}
