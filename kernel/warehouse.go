package kernel

import (
	"fmt"
	"strings"
)

// DecorIDSep separates a decor name from its carton name in a decor ID.
const DecorIDSep = "@"

// Warehouse is a registry for cartons and decor definitions.
type Warehouse struct {
	// Burrow owns the Warehouse.
	Burrow *Burrow

	// CartonsByNames stores registered cartons by name.
	CartonsByNames map[string]*Carton

	// DecorDefsByIDs stores registered decor definitions by decor ID.
	DecorDefsByIDs map[string]*DecorDef
}

// NewWarehouse returns a Warehouse for burrow.
func NewWarehouse(burrow *Burrow) *Warehouse {
	return &Warehouse{
		Burrow:         burrow,
		CartonsByNames: make(map[string]*Carton),
		DecorDefsByIDs: make(map[string]*DecorDef),
	}
}

// GetDecorID returns the decor ID for decorName in cartonName.
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

// RegisterCarton validates carton and registers its decor definitions.
func (w *Warehouse) RegisterCarton(carton *Carton) error {
	cartonName := carton.Name()
	if cartonName == "" {
		return fmt.Errorf("carton name is empty")
	}

	if _, exists := w.CartonsByNames[cartonName]; exists {
		return fmt.Errorf("carton with name %q already exists", cartonName)
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

	for decorName, decorDef := range carton.DecorDefsByNames {
		if decorDef == nil {
			decorID := GetDecorID(decorName, cartonName)
			return fmt.Errorf(
				"decor definition for decor %q in carton %q is nil",
				decorID,
				cartonName,
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
