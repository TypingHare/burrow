package kernel

import "fmt"

// Warehouse is responsible for managing the cartons of the burrow.
type Warehouse struct {
	// burrow is the Burrow that this Warehouse manages.
	burrow *Burrow

	// decorationFactoryMap is a map of decoration IDs to corresponding
	// factories.
	decorationFactoryMap map[string]DecorationFactory

	// cartonMap is a map of carton names to corresponding carton pointers.
	cartonMap map[string]*Carton
}

// NewWarehouse creates a new warehouse instance with the given burrow.
func NewWarehouse(burrow *Burrow) *Warehouse {
	return &Warehouse{
		burrow:               burrow,
		decorationFactoryMap: make(map[string]DecorationFactory),
		cartonMap:            make(map[string]*Carton),
	}
}

// Burrow returns the burrow of the warehouse.
func (w *Warehouse) Burrow() *Burrow {
	return w.burrow
}

// DecorationFactoryMap returns the decoration factory map of the warehouse.
func (w *Warehouse) DecorationFactoryMap() map[string]DecorationFactory {
	return w.decorationFactoryMap
}

// CartonMap returns the carton map of the warehouse.
func (w *Warehouse) CartonMap() map[string]*Carton {
	return w.cartonMap
}

func (w *Warehouse) RegisterCarton(carton *Carton) error {
	cartonName := carton.Metadata.Get(MetadataName)
	if cartonName == "" {
		return fmt.Errorf("carton name is empty")
	}

	if _, exists := w.cartonMap[cartonName]; exists {
		return fmt.Errorf("carton with name %s already exists", cartonName)
	}

	for decorationName, decorationFactory := range carton.decorationFactoryMap {
		if decorationFactory == nil {
			return fmt.Errorf(
				"decoration factory for %s in carton %s is nil",
				decorationName,
				cartonName,
			)
		}

		if w.decorationFactoryMap[decorationName] != nil {
			return fmt.Errorf(
				"decoration factory for %s in carton %s already exists in "+
					"warehouse",
				decorationName,
				cartonName,
			)
		}

	}

	for decorationName, decorationFactory := range carton.decorationFactoryMap {
		decorationId := decorationName + "@" + cartonName
		w.decorationFactoryMap[decorationId] = decorationFactory
	}

	w.cartonMap[cartonName] = carton

	return nil
}
