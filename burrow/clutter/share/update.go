package share

import (
	"errors"
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// BurrowSelfUpdate fast-forwards the Burrow source checkout, selects the
// latest release tag for the current major.minor line, and checks it out.
func BurrowSelfUpdate(
	burrowSourceDir string,
	binDir string,
	cartonDefs []*CartonDef,
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

	originalCommit, err := GitGetCurrentCommit(burrowSourceDir)
	if err != nil {
		return "", fmt.Errorf(
			"failed to determine current Burrow commit: %w",
			err,
		)
	}

	err = GitPullLatestChanges(burrowSourceDir)
	if err != nil {
		return originalCommit, fmt.Errorf(
			"failed to pull latest changes for Burrow source: %w",
			err,
		)
	}

	majorMinorVersion := kernel.GetBurrowMajorMinorVersion()
	tagPattern := fmt.Sprintf("v%s.*", majorMinorVersion)
	tags, err := GitGetTagList(burrowSourceDir, tagPattern)
	if len(tags) == 0 {
		return originalCommit, fmt.Errorf(
			"no Burrow release tags found matching %q",
			tagPattern,
		)
	}
	targetTag := tags[0]

	err = GitCheckoutTag(burrowSourceDir, targetTag)
	if err != nil {
		return originalCommit, fmt.Errorf(
			"failed to check out Burrow release %q: %w",
			targetTag,
			err,
		)
	}

	return originalCommit, nil
}
