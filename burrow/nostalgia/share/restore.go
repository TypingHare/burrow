package share

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// Restore copies a timestamped nostalgia backup back into the chamber's
// blueprint and data locations.
func Restore(chamber *kernel.Chamber, datetimeString string) error {
	if _, err := GetDatetimeFromString(datetimeString); err != nil {
		return fmt.Errorf(
			"invalid backup datetime string %q: %w",
			datetimeString,
			err,
		)
	}

	configBackupDir := GetConfigBackupDir(chamber, datetimeString)
	dataBackupDir := GetDataBackupDir(chamber, datetimeString)

	// Check that the config backup directory exists before restoring from it.
	_, err := os.Stat(configBackupDir)
	if err != nil {
		if os.IsNotExist(err) {
			return fmt.Errorf(
				"config backup directory does not exist: %s",
				configBackupDir,
			)
		}
		return fmt.Errorf("failed to check config backup directory: %w", err)
	}

	// Check that the data backup directory exists before restoring from it.
	_, err = os.Stat(dataBackupDir)
	if err != nil {
		if os.IsNotExist(err) {
			return fmt.Errorf(
				"data backup directory does not exist: %s",
				dataBackupDir,
			)
		}
		return fmt.Errorf("failed to check data backup directory: %w", err)
	}

	blueprintFilePath := chamber.GetBlueprintPath()
	err = os.MkdirAll(filepath.Dir(blueprintFilePath), 0o755)
	if err != nil {
		return fmt.Errorf("failed to ensure blueprint directory: %w", err)
	}

	// Restore the backed-up blueprint file into the chamber config directory.
	blueprintBackupPath := filepath.Join(
		configBackupDir,
		filepath.Base(blueprintFilePath),
	)
	err = CopyPath(blueprintBackupPath, blueprintFilePath)
	if err != nil {
		return fmt.Errorf(
			"failed to restore blueprint file %q from backup: %w",
			blueprintFilePath,
			err,
		)
	}

	err = os.MkdirAll(chamber.GetDataDir(), 0o755)
	if err != nil {
		return fmt.Errorf("failed to ensure chamber data directory: %w", err)
	}

	// Restore all backed-up data entries into the chamber data directory.
	dataEntries, err := os.ReadDir(dataBackupDir)
	if err != nil {
		return fmt.Errorf("failed to read data backup directory: %w", err)
	}
	for _, dataEntry := range dataEntries {
		sourcePath := filepath.Join(dataBackupDir, dataEntry.Name())
		targetPath := filepath.Join(chamber.GetDataDir(), dataEntry.Name())
		err = CopyPath(sourcePath, targetPath)
		if err != nil {
			return fmt.Errorf(
				"failed to restore %q from backup: %w",
				targetPath,
				err,
			)
		}
	}

	return nil
}
