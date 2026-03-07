package share

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

const NostalgiaDirName = "nostalgia"

func GetNostalgiaConfigDir(chamber *kernel.Chamber) string {
	return filepath.Join(chamber.GetConfigDir(), NostalgiaDirName)
}

func GetNostalgiaDataDir(chamber *kernel.Chamber) string {
	return filepath.Join(chamber.GetDataDir(), NostalgiaDirName)
}

func GetConfigBackupDir(chamber *kernel.Chamber, datetimeString string) string {
	return filepath.Join(GetNostalgiaConfigDir(chamber), datetimeString)
}

func GetDataBackupDir(chamber *kernel.Chamber, datetimeString string) string {
	return filepath.Join(GetNostalgiaDataDir(chamber), datetimeString)
}

// CopyPath recursively copies sourcePath to targetPath and fails if targetPath
// already exists.
func CopyPath(sourcePath string, targetPath string) error {
	_, err := os.Stat(targetPath)
	if err != nil && !os.IsNotExist(err) {
		return fmt.Errorf("failed to check target path %q: %w", targetPath, err)
	} else if err == nil {
		return fmt.Errorf("target path already exists: %s", targetPath)
	}

	sourceInfo, err := os.Stat(sourcePath)
	if err != nil {
		return fmt.Errorf("failed to stat source path %q: %w", sourcePath, err)
	}

	if sourceInfo.IsDir() {
		if err := os.MkdirAll(
			targetPath,
			sourceInfo.Mode().Perm(),
		); err != nil {
			return fmt.Errorf(
				"failed to create target directory %q: %w",
				targetPath,
				err,
			)
		}

		entries, err := os.ReadDir(sourcePath)
		if err != nil {
			return fmt.Errorf(
				"failed to read source directory %q: %w",
				sourcePath,
				err,
			)
		}

		for _, entry := range entries {
			childSourcePath := filepath.Join(sourcePath, entry.Name())
			childTargetPath := filepath.Join(targetPath, entry.Name())
			if err := CopyPath(childSourcePath, childTargetPath); err != nil {
				return err
			}
		}

		return nil
	}

	data, err := os.ReadFile(sourcePath)
	if err != nil {
		return fmt.Errorf("failed to read source file %q: %w", sourcePath, err)
	}

	if err := os.WriteFile(
		targetPath,
		data,
		sourceInfo.Mode().Perm(),
	); err != nil {
		return fmt.Errorf(
			"failed to write %q from %q: %w",
			targetPath,
			sourcePath,
			err,
		)
	}

	return nil
}
