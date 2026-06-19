package share

import (
	"errors"
	"fmt"
	"os"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetCreatedChamberNames returns the names of all in-memory chambers.
func GetCreatedChamberNames(burrow *kernel.Burrow) []string {
	chambersByNames := burrow.Architect.ChambersByNames
	chamberNames := make([]string, 0, len(chambersByNames))
	for chamberName := range chambersByNames {
		chamberNames = append(chamberNames, chamberName)
	}

	return chamberNames
}

// GetAllChamberNames returns the names of all chambers, each corresponding to a
// folder in the Burrow configuration directory.
func GetAllChamberNames(burrow *kernel.Burrow) ([]string, error) {
	configDir := burrow.GetConfigDir()
	entries, err := os.ReadDir(configDir)
	if errors.Is(err, os.ErrNotExist) {
		return []string{}, nil
	}
	if err != nil {
		return nil, fmt.Errorf(
			"failed to read configuration directory %q: %w",
			configDir,
			err,
		)
	}

	chamberNames := []string{}
	for _, entry := range entries {
		if entry.IsDir() {
			chamberNames = append(chamberNames, entry.Name())
		}
	}

	// Add root chamber name if it doesn't exist in chamberNames.
	rootChamberName := burrow.Env.Get(kernel.EnvRootChamberName)
	if !slices.Contains(chamberNames, rootChamberName) {
		chamberNames = append(chamberNames, rootChamberName)
	}

	// Sort chamber names alphabetically.
	slices.Sort(chamberNames)

	return chamberNames, nil
}
