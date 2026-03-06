package share

import (
	"fmt"
	"maps"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// Redig rebuilds chamber in place by burying its current instance and digging
// a fresh one from the persisted blueprint.
func Redig(chamber *kernel.Chamber) (*kernel.Chamber, error) {
	architect := chamber.Burrow().Architect()

	if err := architect.Bury(chamber.Name()); err != nil {
		return nil, fmt.Errorf("failed to redig the chamber: %w", err)
	}

	newChamber, err := architect.Dig(chamber.Name())
	if err != nil {
		return nil, fmt.Errorf("failed to redig the chamber: %w", err)
	}

	return newChamber, nil
}

// UpdateBlueprintAndRedig updates the chamber's blueprint using the provided
// update function and then attempts to redig the chamber. If redigging fails,
// it reverts the blueprint to its original state and tries to dig again.
//
// Since this function only shallow-copies the blueprint map, updateFunc must
// replace raw specs rather than mutating nested values in place.
func UpdateBlueprintAndRedig(
	chamber *kernel.Chamber,
	updateFunc func(kernel.Blueprint) error,
) (*kernel.Chamber, error) {
	architect := chamber.Burrow().Architect()
	chamberName := chamber.Name()
	blueprintPath := architect.GetBlueprintPath(chamberName)
	originalBlueprint := maps.Clone(chamber.Blueprint())

	err := updateFunc(chamber.Blueprint())
	if err != nil {
		return nil, fmt.Errorf("failed to update blueprint: %w", err)
	}

	newChamber, err := Redig(chamber)
	if err != nil {
		// Revert the blueprint if redigging fails.
		if err := originalBlueprint.SaveToJSONFile(blueprintPath); err != nil {
			return nil, fmt.Errorf(
				"failed to revert blueprint after redig failure: %w",
				err,
			)
		}

		if _, err := architect.Dig(chamberName); err != nil {
			return nil, fmt.Errorf(
				"failed to revert blueprint after redig failure: %w",
				err,
			)
		}

		return nil, fmt.Errorf(
			"failed to redig chamber after adding decoration: %w",
			err,
		)
	}

	return newChamber, nil
}
