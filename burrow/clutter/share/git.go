package share

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
)

func gitClone(URL string, dir string) error {
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
