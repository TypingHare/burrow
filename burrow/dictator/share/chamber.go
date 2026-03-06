package share

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// chamberNamePattern keeps chamber names flat within the Burrow config and
// data directories by allowing only alphanumerics, underscores, and dots.
var chamberNamePattern = regexp.MustCompile(`^[A-Za-z0-9_.]*[A-Za-z0-9_.]$`)

// isValidChamberName reports whether chamberName uses only supported chamber
// name characters and is not one of Burrow's reserved dot names.
func isValidChamberName(chamberName string) bool {
	if chamberName == "" || chamberName == "." || chamberName == ".." {
		return false
	}

	return chamberNamePattern.MatchString(chamberName)
}

// CreateChamber creates a new chamber blueprint when the target chamber does
// not already exist. The initial blueprint contains only the core decoration.
func CreateChamber(chamber *kernel.Chamber, chamberName string) error {
	chamberName = strings.TrimSpace(chamberName)
	if !isValidChamberName(chamberName) {
		return fmt.Errorf("invalid chamber name: %q", chamberName)
	}

	architect := chamber.Burrow().Architect()
	blueprintPath := architect.GetBlueprintPath(chamberName)

	_, err := os.Stat(blueprintPath)
	if err == nil {
		return fmt.Errorf("chamber %q already exists", chamberName)
	}

	if !errors.Is(err, os.ErrNotExist) {
		return fmt.Errorf(
			"failed to check chamber %q blueprint: %w",
			chamberName,
			err,
		)
	}

	blueprint := kernel.Blueprint{
		kernel.GetDecorationID("core", kernel.CartonName): kernel.NewRawSpec(),
	}
	if err := architect.SaveBlueprint(chamberName, blueprint); err != nil {
		return fmt.Errorf("failed to create chamber: %w", err)
	}

	return nil
}

// DestroyChamber removes a chamber from disk. If the chamber is currently dug,
// it is buried first to persist state and stop running decorations.
//
// The chamber data directory is derived from the chamber name instead of any
// in-memory chamber instance so destroy also works for chambers that exist only
// on disk.
func DestroyChamber(chamber *kernel.Chamber, chamberName string) error {
	architect := chamber.Burrow().Architect()

	// Bury the chamber if it is currently dug to persist state and stop running
	// decorations.
	_, exists := architect.ChamberMap()[chamberName]
	if exists {
		if err := architect.Bury(chamberName); err != nil {
			return fmt.Errorf("failed to bury chamber before destroy: %w", err)
		}
	}

	blueprintPath := architect.GetBlueprintPath(chamberName)
	_, err := os.Stat(blueprintPath)
	if errors.Is(err, os.ErrNotExist) {
		return fmt.Errorf("chamber %q does not exist", chamberName)
	}
	if err != nil {
		return fmt.Errorf(
			"failed to check chamber %q blueprint: %w",
			chamberName,
			err,
		)
	}

	if err := os.Remove(blueprintPath); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q blueprint at %q: %w",
			chamberName,
			blueprintPath,
			err,
		)
	}

	chamberDataDir := filepath.Join(
		chamber.Burrow().GetChamberDir(),
		chamberName,
	)
	if err := os.RemoveAll(chamberDataDir); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q data directory %q: %w",
			chamberName,
			chamberDataDir,
			err,
		)
	}

	return nil
}

// GetDugChamberNames returns the names of all currently dug chambers.
func GetDugChamberNames(burrow *kernel.Burrow) []string {
	chamberMap := burrow.Architect().ChamberMap()
	chamberNames := make([]string, 0, len(chamberMap))
	for chamberName := range chamberMap {
		chamberNames = append(chamberNames, chamberName)
	}

	return chamberNames
}

// GetAllChamberNames returns the names of all chambers, each corresponding to a
// folder in the Burrow config directory.
func GetAllChamberNames(burrow *kernel.Burrow) ([]string, error) {
	configDir := burrow.GetConfigDir()
	entries, err := os.ReadDir(configDir)
	if errors.Is(err, os.ErrNotExist) {
		return []string{}, nil
	}
	if err != nil {
		return nil, fmt.Errorf(
			"failed to read config directory %q: %w",
			configDir,
			err,
		)
	}

	chamberNames := make([]string, 0, len(entries))
	for _, entry := range entries {
		if entry.IsDir() {
			chamberNames = append(chamberNames, entry.Name())
		}
	}
	slices.Sort(chamberNames)

	return chamberNames, nil
}
