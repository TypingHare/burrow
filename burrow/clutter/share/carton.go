package share

import (
	"fmt"
	"path/filepath"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// InstallCarton installs a carton with the given name and path to the Chamber's
// Burrow.
func InstallCarton(
	chamber *kernel.Chamber,
	spec ClutterSpec,
	cartonName string,
	path string,
) error {
	originalCartonNames := slices.Clone(spec.CartonNames)
	originalLocalCartons := slices.Clone(spec.LocalCartons)

	spec.CartonNames = append(spec.CartonNames, cartonName)

	if path != "" {
		// The carton is a local repository.
		spec.LocalCartons = append(spec.LocalCartons, LocalCarton{
			Name: cartonName,
			Path: path,
		})
	} else {
		// The carton is a remote repository.
		sourceDir := filepath.Join(
			chamber.Burrow().GetSourceDir(),
			cartonName,
		)
		remoteRepositoryURL := "https://" + cartonName
		_, stderr, exitCode, err := share.RunExternalCommand(
			"",
			[]string{
				"git", "clone", remoteRepositoryURL, sourceDir,
			},
		)
		if err != nil || exitCode != 0 {
			return fmt.Errorf(
				"failed to clone carton repository: %q: %w",
				cartonName,
				fmt.Errorf("%s", stderr),
			)
		}
	}

	// Attempt to build the Burrow executable with the new carton.
	err := BuildBurrowStandard(
		chamber.Burrow(),
		spec.CartonNames,
		spec.LocalCartons,
	)
	if err != nil {
		// Roll back the changes to spec.
		spec.CartonNames = originalCartonNames
		spec.LocalCartons = originalLocalCartons

		return fmt.Errorf("failed to build burrow: %w", err)
	}

	// Save the blueprint.
	err = chamber.SaveBlueprint()
	if err != nil {
		return fmt.Errorf("failed to save blueprint after building "+
			"Burrow executable: %w", err)
	}

	return nil
}

// UninstallCarton uninstalls a carton with the given name and path from the
// Chamber's Burrow.
func UninstallCarton(
	chamber *kernel.Chamber,
	spec ClutterSpec,
	cartonName string,
) error {
	return nil
}
