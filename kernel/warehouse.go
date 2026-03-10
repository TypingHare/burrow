package kernel

import (
	"fmt"
	"strings"
)

// Warehouse manages a Burrow's cartons and decor factories.
type Warehouse struct {
	// burrow is the Burrow managed by the Warehouse.
	burrow *Burrow

	// decorDefsByIDs stores decor factories by decor ID.
	decorDefsByIDs map[string]DecorDef

	// cartonsByNames stores cartons by name.
	cartonsByNames map[string]*Carton
}

// NewWarehouse returns a Warehouse for burrow.
func NewWarehouse(burrow *Burrow) *Warehouse {
	return &Warehouse{
		burrow:         burrow,
		decorDefsByIDs: make(map[string]DecorDef),
		cartonsByNames: make(map[string]*Carton),
	}
}

// Burrow returns the Burrow managed by the Warehouse.
func (w *Warehouse) Burrow() *Burrow {
	return w.burrow
}

func (w *Warehouse) DecorDefsByNames() map[string]DecorDef {
	return w.decorDefsByIDs
}

func (w *Warehouse) CartonsByNames() map[string]*Carton {
	return w.cartonsByNames
}

// GetCarton returns the carton named name.
func (w *Warehouse) GetCarton(name string) (*Carton, error) {
	carton, exists := w.cartonsByNames[name]
	if !exists || carton == nil {
		return nil, fmt.Errorf("carton with name %q does not exist", name)
	}

	return carton, nil
}

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

	if _, exists := w.cartonsByNames[cartonName]; exists {
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
		w.decorDefsByIDs[decorID] = decorDef
	}

	w.cartonsByNames[cartonName] = carton

	return nil
}

func GetDecorID(decorName string, cartonName string) string {
	return decorName + DecorIDSep + cartonName
}

func SplitDecorID(decorID string) (string, string, error) {
	parts := strings.Split(decorID, DecorIDSep)
	if len(parts) != 2 {
		return "", "", fmt.Errorf("invalid decor ID: %q", decorID)
	}

	return parts[0], parts[1], nil
}
