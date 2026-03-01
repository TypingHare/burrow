package share

import "github.com/TypingHare/burrow/v2026/kernel"

// Redig performs a redig operation on the chamber, which involves burying and
// then digging it again.
func Redig(chamber *kernel.Chamber) error {
	architect := chamber.Burrow().Architect()

	if err := architect.Bury(chamber.Name()); err != nil {
		return chamber.Error("failed to redig the chamber: %v", err)
	}

	if _, err := architect.Dig(chamber.Name()); err != nil {
		return chamber.Error("failed to redig the chamber: %v", err)
	}

	return nil
}

// UpdateBlueprintAndRedig updates the chamber's blueprint using the provided
// update function and then attempts to redig the chamber. If redigging fails,
// it reverts the blueprint to its original state and tries to dig again.
func UpdateBlueprintAndRedig(
	chamber *kernel.Chamber,
	updateFunc func(kernel.Blueprint) error,
) error {
	architect := chamber.Burrow().Architect()
	chamberName := chamber.Name()
	blueprintPath := architect.GetBlueprintPath(chamberName)
	originalBlueprint := chamber.Blueprint()

	err := updateFunc(chamber.Blueprint())
	if err != nil {
		return chamber.Error("failed to update blueprint: %v", err)
	}

	if err := Redig(chamber); err != nil {
		// Revert the blueprint if redigging fails.
		originalBlueprint.SaveToJSONFile(blueprintPath)
		if _, err := architect.Dig(chamberName); err != nil {
			return chamber.Error(
				"failed to revert blueprint after redig failure: %v",
				err,
			)
		}

		return chamber.Error(
			"failed to redig chamber after adding decoration: %v",
			err,
		)
	}

	return nil
}
