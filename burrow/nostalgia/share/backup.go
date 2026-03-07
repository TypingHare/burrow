package share

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// Backup copies the chamber's blueprint and data into timestamped nostalgia
// backup directories.
func Backup(chamber *kernel.Chamber) error {
	datetimeString := GetCurrentDatetimeString()
	configBackupDir := GetConfigBackupDir(chamber, datetimeString)
	dataBackupDir := GetDataBackupDir(chamber, datetimeString)

	// Check and ensure the config backup directory.
	_, err := os.Stat(configBackupDir)
	if err != nil && !os.IsNotExist(err) {
		return fmt.Errorf("failed to check config backup directory: %w", err)
	} else if err == nil {
		return fmt.Errorf(
			"config backup directory already exists: %s",
			configBackupDir,
		)
	}

	blueprintFilePath := chamber.GetBlueprintPath()
	err = os.MkdirAll(configBackupDir, 0o755)
	if err != nil {
		return fmt.Errorf("failed to create config backup directory: %w", err)
	}

	// Check and ensure the data backup directory.
	_, err = os.Stat(dataBackupDir)
	if err != nil && !os.IsNotExist(err) {
		return fmt.Errorf("failed to check data backup directory: %w", err)
	} else if err == nil {
		return fmt.Errorf(
			"data backup directory already exists: %s",
			dataBackupDir,
		)
	}

	err = os.MkdirAll(dataBackupDir, 0o755)
	if err != nil {
		return fmt.Errorf("failed to create backup directory: %w", err)
	}

	// Copy all files from the chamber's data directory to the backup directory,
	// excluding the nostalgia subdirectory.
	dataEntries, err := os.ReadDir(chamber.GetDataDir())
	if err != nil {
		return fmt.Errorf("failed to read chamber data directory: %w", err)
	}
	for _, dataEntry := range dataEntries {
		if dataEntry.Name() == NostalgiaDirName {
			continue
		}

		sourcePath := filepath.Join(chamber.GetDataDir(), dataEntry.Name())
		targetPath := filepath.Join(dataBackupDir, dataEntry.Name())
		err = CopyPath(sourcePath, targetPath)
		if err != nil {
			return fmt.Errorf(
				"failed to copy %q to backup directory: %w",
				sourcePath,
				err,
			)
		}
	}

	// Copy the blueprint file into the timestamped config backup directory.
	blueprintBackupPath := filepath.Join(
		configBackupDir,
		filepath.Base(blueprintFilePath),
	)
	err = CopyPath(blueprintFilePath, blueprintBackupPath)
	if err != nil {
		return fmt.Errorf(
			"failed to copy blueprint file %q to backup directory: %w",
			blueprintFilePath,
			err,
		)
	}

	return nil
}
