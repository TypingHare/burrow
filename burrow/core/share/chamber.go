package share

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// RecreateChamber attempts to re-create the chamber. It returns the new chamber
// if successful, or an error if re-creation fails.
func RecreateChamber(chamber *kernel.Chamber) (*kernel.Chamber, error) {
	chamber.Burrow.Architect.Delete(chamber.Name)
	newChamber, err := chamber.Burrow.Architect.Create(chamber.Name)
	if err != nil {
		return nil, fmt.Errorf("failed to re-create chamber: %w", err)
	}

	return newChamber, nil
}

// UpdateBlueprintAndReCreate updates the chamber's blueprint using the provided
// update function and then attempts to re-create the chamber. If re-creation
// fails, it reverts the blueprint to its original state and tries to create
// again.
func UpdateBlueprintAndReCreate(
	chamber *kernel.Chamber,
	updateFunc func(kernel.Blueprint) error,
) (*kernel.Chamber, error) {
	architect := chamber.Burrow.Architect
	chamberName := chamber.Name
	blueprintPath := architect.GetBlueprintPath(chamberName)
	clonedBlueprint := DeepCopyBlueprint(chamber.Blueprint)

	// Update the blueprint using the provided function.
	err := updateFunc(chamber.Blueprint)
	if err != nil {
		return nil, fmt.Errorf("failed to update blueprint: %w", err)
	}

	newChamber, err := RecreateChamber(chamber)
	if err != nil {
		// Revert the blueprint if re-creation fails.
		if err := clonedBlueprint.SaveToTomlFile(blueprintPath); err != nil {
			return nil, fmt.Errorf(
				"failed to revert blueprint after re-creation failure: %w",
				err,
			)
		}

		// Attempt to re-create the chamber with the original blueprint.
		if _, err := architect.Create(chamberName); err != nil {
			return nil, fmt.Errorf(
				"failed to revert blueprint after re-creation failure: %w",
				err,
			)
		}

		return nil, fmt.Errorf(
			"failed to re-create chamber after adding decor: %w",
			err,
		)
	}

	return newChamber, nil
}

// IsRoot checks if the given chamber is the root chamber of the burrow.
func IsRoot(chamber *kernel.Chamber) bool {
	return chamber.Name == chamber.Burrow.Env.Get(kernel.EnvRootChamberName)
}
