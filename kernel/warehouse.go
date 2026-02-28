package kernel

import (
	"fmt"
	"strings"
)

// Warehouse manages a Burrow's cartons and decoration factories.
type Warehouse struct {
	// burrow is the Burrow managed by the Warehouse.
	burrow *Burrow

	// decorationFactoryMap stores decoration factories by decoration ID.
	decorationFactoryMap map[string]DecorationFactory

	// cartonMap stores cartons by name.
	cartonMap map[string]*Carton
}

// NewWarehouse returns a Warehouse for burrow.
func NewWarehouse(burrow *Burrow) *Warehouse {
	return &Warehouse{
		burrow:               burrow,
		decorationFactoryMap: make(map[string]DecorationFactory),
		cartonMap:            make(map[string]*Carton),
	}
}

// Burrow returns the Burrow managed by the Warehouse.
func (w *Warehouse) Burrow() *Burrow {
	return w.burrow
}

// DecorationFactoryMap returns decoration factories keyed by decoration ID.
func (w *Warehouse) DecorationFactoryMap() map[string]DecorationFactory {
	return w.decorationFactoryMap
}

// CartonMap returns cartons keyed by name.
func (w *Warehouse) CartonMap() map[string]*Carton {
	return w.cartonMap
}

// GetCarton returns the carton named name.
func (w *Warehouse) GetCarton(name string) (*Carton, error) {
	carton, exists := w.cartonMap[name]
	if !exists {
		return nil, fmt.Errorf("carton with name %q does not exist", name)
	}

	return carton, nil
}

// GetDecorationID returns the decoration ID for decorationName in cartonName.
func (w *Warehouse) GetDecorationID(
	decorationName string,
	cartonName string,
) string {
	decorationIDSep := w.burrow.Env.Get(EnvDecorationIDSep)
	return decorationName + decorationIDSep + cartonName
}

// SplitDecorationID splits decorationID into its decoration and carton names.
func (w *Warehouse) SplitDecorationID(
	decorationID string,
) (string, string, error) {
	decorationIDSep := w.burrow.Env.Get(EnvDecorationIDSep)
	parts := strings.Split(decorationID, decorationIDSep)
	if len(parts) != 2 {
		return "", "", fmt.Errorf("invalid decoration ID: %q", decorationID)
	}

	return parts[0], parts[1], nil
}

// RegisterCarton registers carton and makes its decoration factories available.
//
// RegisterCarton returns an error if the carton has no name, if a carton with
// the same name is already registered, or if any decoration factory is nil or
// conflicts with an existing registration.
func (w *Warehouse) RegisterCarton(carton *Carton) error {
	cartonName := carton.Metadata.Get(MetadataName)
	if cartonName == "" {
		return fmt.Errorf("carton name is empty")
	}

	cartonVersion := carton.Metadata.Get(MetadataVersion)
	if cartonVersion == "" {
		return fmt.Errorf("carton version is empty")
	}

	cartonMajorMinorVersion, err := GetMajorMinorVersion(cartonVersion)
	if err != nil {
		return fmt.Errorf("invalid carton version %q: %w", cartonVersion, err)
	}

	burrowMajorMinorVersion, err := GetMajorMinorVersion(Version)
	if err != nil {
		return fmt.Errorf("invalid burrow version %q: %w", Version, err)
	}

	if cartonMajorMinorVersion != burrowMajorMinorVersion {
		return fmt.Errorf(
			"carton version %q is incompatible with burrow version %q",
			cartonVersion,
			Version,
		)
	}

	if _, exists := w.cartonMap[cartonName]; exists {
		return fmt.Errorf("carton with name %q already exists", cartonName)
	}

	for decorationName, decorationFactory := range carton.decorationFactoryMap {
		decorationID := w.GetDecorationID(decorationName, cartonName)
		if decorationFactory == nil {
			return fmt.Errorf(
				"decoration factory for decoration %q is nil",
				decorationID,
			)
		}

		if w.decorationFactoryMap[decorationName] != nil {
			return fmt.Errorf(
				"decoration factory for %q already exists in warehouse",
				decorationID,
			)
		}

	}

	for decorationName, decorationFactory := range carton.decorationFactoryMap {
		decorationID := w.GetDecorationID(decorationName, cartonName)
		w.decorationFactoryMap[decorationID] = decorationFactory
	}

	w.cartonMap[cartonName] = carton

	return nil
}

// GetDecorationFactory returns the factory for decorationID.
func (w *Warehouse) GetDecorationFactory(
	decorationID string,
) (DecorationFactory, error) {
	decorationFactory, exists := w.decorationFactoryMap[decorationID]
	if !exists {
		return nil, fmt.Errorf(
			"decoration factory with ID %q does not exist",
			decorationID,
		)
	}
	return decorationFactory, nil
}
