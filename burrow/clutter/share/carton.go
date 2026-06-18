package share

import (
	"fmt"
	"slices"
)

// CartonDef defines a carton to be installed in a chamber.
type CartonDef struct {
	// Name is the name of the carton as defined in the chamber spec.
	Name string

	// Path is the local filesystem path to the carton checkout. If it is empty,
	// the carton will be downloaded.
	Path string
}

// InstallCarton adds a carton definition to the decor and updates the spec.
func InstallCarton(
	decor IDecor,
	cartonName string,
	cartonPath string,
) error {
	cartons := decor.CartonDefs()
	originalCartons := slices.Clone(cartons)
	rollback := func() {
		decor.SetCartonDefs(originalCartons)
	}

	// Add the new carton definition to the decor's list of carton definitions.
	decor.SetCartonDefs(append(cartons, &CartonDef{
		Name: cartonName,
		Path: cartonPath,
	}))

	// Save the blueprint.
	err := decor.UpdateSpec()
	if err != nil {
		rollback()
		return fmt.Errorf(
			"failed to update spec after installing carton %q: %w",
			cartonName,
			err,
		)
	}

	// TODO: re-build the burrow so that it can load the decors in the carton.

	return nil
}

// UninstallCarton removes a carton definition from the decor and updates the
// spec.
func UninstallCarton(
	decor IDecor,
	cartonName string,
) error {
	cartons := decor.CartonDefs()
	originalCartons := slices.Clone(cartons)
	rollback := func() {
		decor.SetCartonDefs(originalCartons)
	}

	// Remove the carton definition from the decor's list of carton definitions.
	filteredCartons := make([]*CartonDef, 0, len(cartons))
	for _, carton := range cartons {
		if carton.Name != cartonName {
			filteredCartons = append(filteredCartons, carton)
		}
	}
	decor.SetCartonDefs(filteredCartons)

	// Update the spec.
	err := decor.UpdateSpec()
	if err != nil {
		rollback()
		return fmt.Errorf(
			"failed to update spec after uninstalling carton %q: %w",
			cartonName,
			err,
		)
	}

	return nil
}

// CartonNames returns the names of the cartons in the given list of carton
// definitions.
func CartonNames(cartons []*CartonDef) []string {
	names := make([]string, 0, len(cartons))

	for _, carton := range cartons {
		names = append(names, carton.Name)
	}

	return names
}
