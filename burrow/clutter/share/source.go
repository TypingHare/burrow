package share

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetCartonSourceDir returns the expected checkout directory for cartonName in
// burrow's source tree.
func GetCartonSourceDir(burrow *kernel.Burrow, cartonName string) string {
	return filepath.Join(burrow.GetSourceDir(), cartonName)
}

// EnsureSourceDir verifies that the checkout directory for cartonName already
// exists.
func EnsureSourceDir(burrow *kernel.Burrow, cartonName string) error {
	sourceDir := GetCartonSourceDir(burrow, cartonName)
	_, err := os.Stat(sourceDir)
	if errors.Is(err, os.ErrNotExist) {
		return fmt.Errorf(
			"Source directory for carton %q does not exist: %s",
			cartonName,
			sourceDir,
		)
	} else if err != nil {
		return fmt.Errorf(
			"Failed to access source directory for carton %q: %s",
			cartonName,
			sourceDir,
		)
	}

	return nil
}
