package kernel

import (
	"fmt"
	"strings"
)

// DecorID is a unique identifier for a decor, composed of the decor name and
// the carton name.
const DecorIDSep = "@"

// Warehouse is a registry for cartons and decor definitions.
type Warehouse struct {
	Burrow         *Burrow
	CartonsByNames map[string]*Carton
	DecorDefsByIDs map[string]DecorDef
}

// NewWarehouse creates a new warehouse for the given burrow.
func NewWarehouse(burrow *Burrow) *Warehouse {
	return &Warehouse{
		Burrow:         burrow,
		CartonsByNames: make(map[string]*Carton),
		DecorDefsByIDs: make(map[string]DecorDef),
	}
}

// RegisterCarton registers a carton in the warehouse. It validates the carton's
// name and version, and ensures that the carton is compatible with the burrow's
// version. It also registers the decor definitions of the carton in the
// warehouse.
func (w *Warehouse) RegisterCarton(carton *Carton) error {
	cartonName := carton.Name()
	if cartonName == "" {
		return fmt.Errorf("carton name is empty")
	}

	cartonVersion := carton.Version()
	if cartonVersion == "" {
		return fmt.Errorf("carton version is empty")
	}

	cartonMajorMinorVersion, err := GetMajorMinorVersion(cartonVersion)
	if err != nil {
		return fmt.Errorf("invalid carton version %q: %w", cartonVersion, err)
	}

	if cartonMajorMinorVersion != GetBurrowMajorMinorVersion() {
		return fmt.Errorf(
			"carton version %q is incompatible with burrow version %q",
			cartonVersion,
			Version,
		)
	}

	if _, exists := w.CartonsByNames[cartonName]; exists {
		return fmt.Errorf("carton with name %q already exists", cartonName)
	}

	for decorName, decorDef := range carton.DecorDefsByNames {
		decorID := GetDecorID(decorName, cartonName)
		if decorDef == nil {
			return fmt.Errorf(
				"decor definition for decor %q is nil",
				decorID,
			)
		}
	}

	for decorName, decorDef := range carton.DecorDefsByNames {
		decorID := GetDecorID(decorName, cartonName)
		w.DecorDefsByIDs[decorID] = decorDef
	}

	w.CartonsByNames[cartonName] = carton

	return nil
}

// GetDecorDef retrieves a decor definition from the warehouse by its decor ID.
func GetDecorID(decorName string, cartonName string) string {
	return decorName + DecorIDSep + cartonName
}

// SplitDecorID splits a decor ID into its decor name and carton name
// components.
func SplitDecorID(decorID string) (string, string, error) {
	parts := strings.Split(decorID, DecorIDSep)
	if len(parts) != 2 {
		return "", "", fmt.Errorf("invalid decor ID: %q", decorID)
	}

	return parts[0], parts[1], nil
}
