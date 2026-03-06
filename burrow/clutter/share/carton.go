package share

import (
	"fmt"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// InstallCarton adds cartonName to the clutter spec, optionally records a
// local override path, rebuilds Burrow, and persists the updated blueprint.
func InstallCarton(
	chamber *kernel.Chamber,
	spec *ClutterSpec,
	cartonName string,
	path string,
) error {
	originalCartonNames := slices.Clone(spec.CartonNames)
	originalLocalCartons := slices.Clone(spec.LocalCartons)

	rollback := func() {
		spec.CartonNames = originalCartonNames
		spec.LocalCartons = originalLocalCartons
	}

	spec.CartonNames = append(spec.CartonNames, cartonName)

	if path != "" {
		// The carton is a local repository.
		spec.LocalCartons = append(spec.LocalCartons, LocalCarton{
			Name: cartonName,
			Path: path,
		})
	} else {
		// The carton is a remote repository.
		sourceDir := GetCartonSourceDir(chamber.Burrow(), cartonName)
		if err := gitClone("https://"+cartonName, sourceDir); err != nil {
			return fmt.Errorf("failed to clone carton repository: %w", err)
		}
	}

	// Attempt to build the Burrow executable with the new carton.
	// err := NewBuilder()
	err := BuildBurrow(
		chamber.Burrow(),
		spec.CartonNames,
		spec.LocalCartons,
		spec.MagicEnv,
	)
	if err != nil {
		rollback()
		return fmt.Errorf("failed to build burrow: %w", err)
	}

	// Save the blueprint.
	err = chamber.UpdateAndSaveBlueprint()
	if err != nil {
		return fmt.Errorf("failed to save blueprint after building "+
			"Burrow executable: %w", err)
	}

	return nil
}

// UninstallCarton removes cartonName from the clutter spec, rebuilds Burrow,
// and persists the updated blueprint.
func UninstallCarton(
	chamber *kernel.Chamber,
	spec *ClutterSpec,
	cartonName string,
) error {
	originalCartonNames := slices.Clone(spec.CartonNames)
	originalLocalCartons := slices.Clone(spec.LocalCartons)

	rollback := func() {
		spec.CartonNames = originalCartonNames
		spec.LocalCartons = originalLocalCartons
	}

	newCartonNames := []string{}
	for _, name := range spec.CartonNames {
		if name != cartonName {
			newCartonNames = append(newCartonNames, name)
		}
	}
	spec.CartonNames = newCartonNames

	newLocalCartons := []LocalCarton{}
	for _, localCaton := range spec.LocalCartons {
		if localCaton.Name != cartonName {
			newLocalCartons = append(newLocalCartons, localCaton)
		}
	}
	spec.LocalCartons = newLocalCartons

	// Attempt to build the Burrow executable with the new carton.
	// err := NewBuilder()
	err := BuildBurrow(
		chamber.Burrow(),
		spec.CartonNames,
		spec.LocalCartons,
		spec.MagicEnv,
	)
	if err != nil {
		rollback()
		return fmt.Errorf("failed to build burrow: %w", err)
	}

	// Save the blueprint.
	err = chamber.UpdateAndSaveBlueprint()
	if err != nil {
		return fmt.Errorf("failed to save blueprint after building "+
			"Burrow executable: %w", err)
	}

	return nil
}
