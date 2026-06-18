package share

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
)

// GitClone clones URL into dir, creating the parent directory first.
func GitClone(URL string, dir string) error {
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

func GitCheckout(dir string, commit string) error {
	_, stderr, exitCode, err := share.RunExternalCommand(
		dir,
		[]string{"git", "checkout", commit},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to roll back to commit %q: %w",
			commit,
			fmt.Errorf("%s", stderr),
		)
	}

	return nil
}

func GitGetCurrentCommit(dir string) (string, error) {
	stdout, stderr, exitCode, err := share.RunExternalCommand(
		dir,
		[]string{"git", "rev-parse", "HEAD"},
	)
	if err != nil || exitCode != 0 {
		return "", fmt.Errorf(
			"failed to get current commit: %s: %w",
			stderr,
			err,
		)
	}

	return strings.TrimSpace(stdout), nil
}

func GitPullLatestChanges(dir string) error {
	_, stderr, exitCode, err := share.RunExternalCommand(
		dir,
		[]string{"git", "pull", "--ff-only", "--tags"},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to pull latest changes for source: %s: %w",
			stderr,
			err,
		)
	}

	return nil
}

func GitGetTagList(dir string, tagPattern string) ([]string, error) {
	stdout, stderr, exitCode, err := share.RunExternalCommand(
		dir,
		[]string{
			"git",
			"tag",
			"--list",
			tagPattern,
			"--sort=-version:refname",
		},
	)
	if err != nil || exitCode != 0 {
		return nil, fmt.Errorf(
			"failed to list tags: %s: %w",
			stderr,
			err,
		)
	}

	return strings.Split(strings.TrimSpace(stdout), "\n"), nil
}

func GitCheckoutTag(dir string, targetTag string) error {
	_, stderr, exitCode, err := share.RunExternalCommand(
		dir,
		[]string{"git", "checkout", targetTag},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to check out: %s: %w",
			stderr,
			err,
		)
	}

	return nil
}
