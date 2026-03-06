package share

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
)

// gitClone clones URL into dir, creating the parent directory first.
func gitClone(URL string, dir string) error {
	// Ensure the parent directory exists before cloning.
	parentDir := filepath.Dir(dir)
	os.MkdirAll(parentDir, 0o755)

	_, stderr, exitCode, err := share.RunExternalCommand(
		"",
		[]string{"git", "clone", URL, dir},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to clone carton repository: %q",
			fmt.Errorf("%s", stderr),
		)
	}

	return nil
}
