package share

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
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

// CreateChamberBlueprint creates a new chamber blueprint when the target
// chamber does not already exist. The initial blueprint contains only the core
// decor.
func CreateChamberBlueprint(chamber *kernel.Chamber, chamberName string) error {
	chamberName = strings.TrimSpace(chamberName)
	if !isValidChamberName(chamberName) {
		return fmt.Errorf("invalid chamber name: %q", chamberName)
	}

	// Check if the blueprint already exists.
	architect := chamber.Burrow.Architect
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

	// If the blueprint doesn't exist, create a new one with the core decor.
	blueprintDir := filepath.Dir(blueprintPath)
	if err := os.MkdirAll(blueprintDir, 0o755); err != nil {
		return fmt.Errorf(
			"failed to create chamber directory %q: %w",
			blueprintDir,
			err,
		)
	}

	blueprint := kernel.Blueprint{
		kernel.GetDecorID("core", kernel.CartonName): kernel.NewVars(),
	}
	if err := blueprint.SaveToTomlFile(blueprintPath); err != nil {
		return fmt.Errorf("failed to save blueprint: %w", err)
	}

	return nil
}

// DeleteChamberBlueprint removes a chamber configuration directory. If the
// chamber has been created, it is deleted first to persist state and stop
// running decors.
func DeleteChamberBlueprint(chamber *kernel.Chamber, chamberName string) error {
	architect := chamber.Burrow.Architect

	// Delete the chamber first if it has been created to persist state and
	// stop running decors.
	chambersByNames := chamber.Burrow.Architect.ChambersByNames
	_, exists := chambersByNames[chamberName]
	if exists {
		if err := architect.Delete(chamberName); err != nil {
			return fmt.Errorf(
				"failed to delete chamber %q before deleting blueprint: %w",
				chamberName,
				err,
			)
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

	// Remove the blueprint file.
	if err := os.Remove(blueprintPath); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q blueprint at %q: %w",
			chamberName,
			blueprintPath,
			err,
		)
	}

	// Remove the chamber configuration directory.
	chamberConfigDir := filepath.Join(
		chamber.Burrow.GetConfigDir(),
		chamberName,
	)
	if err := os.RemoveAll(chamberConfigDir); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q configuration directory %q: %w",
			chamberName,
			chamberConfigDir,
			err,
		)
	}

	// Remove the chamber data directory.
	chamberDataDir := filepath.Join(
		chamber.Burrow.GetDataDir(),
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

	// Remove the chamber state directory.
	chamberStateDir := filepath.Join(
		chamber.Burrow.GetStateDir(),
		chamberName,
	)
	if err := os.RemoveAll(chamberStateDir); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q state directory %q: %w",
			chamberName,
			chamberStateDir,
			err,
		)
	}

	return nil
}
