package share

import (
	"errors"
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// CreateChamber creates a new chamber blueprint when the target chamber does
// not already exist. The initial blueprint contains only the core decoration.
func CreateChamber(chamber *kernel.Chamber, chamberName string) error {
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
func DestroyChamber(chamber *kernel.Chamber, chamberName string) error {
	architect := chamber.Burrow().Architect()

	chamberToDelete, exists := architect.ChamberMap()[chamberName]
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
			"failed to remove chamber %q blueprint: %w",
			chamberName,
			err,
		)
	}

	chamberDataDir := chamberToDelete.GetDataDir()
	if err := os.RemoveAll(chamberDataDir); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q directory: %w",
			chamberName,
			err,
		)
	}

	return nil
}
