package share

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

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

func DestroyChamber(chamber *kernel.Chamber, chamberName string) error {
	architect := chamber.Burrow().Architect()

	if _, exists := architect.ChamberMap()[chamberName]; exists {
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

	chamberDirPath := filepath.Join(
		chamber.Burrow().GetChamberDir(),
		chamberName,
	)
	if err := os.RemoveAll(chamberDirPath); err != nil {
		return fmt.Errorf(
			"failed to remove chamber %q directory: %w",
			chamberName,
			err,
		)
	}

	return nil
}
