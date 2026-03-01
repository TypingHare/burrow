package share

import (
	"errors"
	"fmt"
	"os"
	"strings"

	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

func BurrowSelfUpdate(
	burrowSourceDir string,
	binDir string,
	cartonNames []string,
	localCartons []LocalCarton,
) (string, error) {
	_, err := os.Stat(burrowSourceDir)
	if errors.Is(err, os.ErrNotExist) {
		return "", fmt.Errorf(
			"Burrow source directory %q does not exist",
			burrowSourceDir,
		)
	} else if err != nil {
		return "", fmt.Errorf(
			"failed to access Burrow source directory %q",
			burrowSourceDir,
		)
	}

	majorMinorVersion, err := kernel.GetMajorMinorVersion(
		kernel.Version,
	)
	if err != nil {
		return "", fmt.Errorf("failed to parse Burrow version")
	}

	tagPattern := fmt.Sprintf("v%s.*", majorMinorVersion)

	stdout, stderr, exitCode, err := coreShare.RunExternalCommand(
		burrowSourceDir,
		[]string{"git", "rev-parse", "HEAD"},
	)
	if err != nil || exitCode != 0 {
		return "", fmt.Errorf(
			"failed to determine current Burrow commit: %s: %w",
			stderr,
			err,
		)
	}
	originalCommit := strings.TrimSpace(stdout)

	_, stderr, exitCode, err = coreShare.RunExternalCommand(
		burrowSourceDir,
		[]string{"git", "pull", "--ff-only", "--tags"},
	)
	if err != nil || exitCode != 0 {
		return originalCommit, fmt.Errorf(
			"failed to pull latest changes for Burrow source: %s: %w",
			stderr,
			err,
		)
	}

	stdout, stderr, exitCode, err = coreShare.RunExternalCommand(
		burrowSourceDir,
		[]string{
			"git",
			"tag",
			"--list",
			tagPattern,
			"--sort=-version:refname",
		},
	)
	if err != nil || exitCode != 0 {
		return originalCommit, fmt.Errorf(
			"failed to list Burrow tags: %s: %w",
			stderr,
			err,
		)
	}

	tags := strings.Fields(stdout)
	if len(tags) == 0 {
		return originalCommit, fmt.Errorf(
			"no Burrow release tags found matching %q",
			tagPattern,
		)
	}

	targetTag := tags[0]

	_, stderr, exitCode, err = coreShare.RunExternalCommand(
		burrowSourceDir,
		[]string{"git", "checkout", targetTag},
	)
	if err != nil || exitCode != 0 {
		return originalCommit, fmt.Errorf(
			"failed to check out Burrow release %q: %s: %w",
			targetTag,
			stderr,
			err,
		)
	}

	return originalCommit, nil
}
