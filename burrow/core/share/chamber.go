package share

import (
	"fmt"
	"maps"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// Redig performs a redig operation on the chamber, which involves burying and
// then digging it again.
func Redig(chamber *kernel.Chamber) error {
	architect := chamber.Burrow().Architect()

	if err := architect.Bury(chamber.Name()); err != nil {
		return fmt.Errorf("failed to redig the chamber: %w", err)
	}

	if _, err := architect.Dig(chamber.Name()); err != nil {
		return fmt.Errorf("failed to redig the chamber: %w", err)
	}

	return nil
}

// UpdateBlueprintAndRedig updates the chamber's blueprint using the provided
// update function and then attempts to redig the chamber. If redigging fails,
// it reverts the blueprint to its original state and tries to dig again.
//
// Since this function only shallow copies the blueprint, it assumes that the
// update function does not any RawSpec objects in the blueprin.
func UpdateBlueprintAndRedig(
	chamber *kernel.Chamber,
	updateFunc func(kernel.Blueprint) error,
) error {
	architect := chamber.Burrow().Architect()
	chamberName := chamber.Name()
	blueprintPath := architect.GetBlueprintPath(chamberName)
	originalBlueprint := maps.Clone(chamber.Blueprint())

	err := updateFunc(chamber.Blueprint())
	if err != nil {
		return fmt.Errorf("failed to update blueprint: %w", err)
	}

	if err := Redig(chamber); err != nil {
		// Revert the blueprint if redigging fails.
		if err := originalBlueprint.SaveToJSONFile(blueprintPath); err != nil {
			return fmt.Errorf(
				"failed to revert blueprint after redig failure: %w",
				err,
			)
		}

		if _, err := architect.Dig(chamberName); err != nil {
			return fmt.Errorf(
				"failed to revert blueprint after redig failure: %w",
				err,
			)
		}

		return fmt.Errorf(
			"failed to redig chamber after adding decoration: %w",
			err,
		)
	}

	return nil
}
