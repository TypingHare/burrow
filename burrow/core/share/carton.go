package share

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
	mapset "github.com/deckarep/golang-set/v2"
)

// GetAllCartonNames returns all carton names in the warehouse, sorted
// alphabetically.
func GetAllCartonNames(warehouse *kernel.Warehouse) []string {
	return slices.Sorted(maps.Keys(warehouse.CartonMap()))
}

// GetCartonNames returns carton names used in the chamber, sorted
// alphabetically.
func GetCartonNames(chamber *kernel.Chamber) ([]string, error) {
	cartonNameSet := mapset.NewSet[string]()
	decorationIDs := chamber.Renovator().OrderedDecorationIDs()
	for _, decorationID := range decorationIDs {
		_, cartonName, err := chamber.Burrow().
			Warehouse().
			SplitDecorationID(decorationID)
		if err != nil {
			return nil, err
		}

		cartonNameSet.Add(cartonName)
	}

	cartonNames := cartonNameSet.ToSlice()
	slices.Sort(cartonNames)

	return cartonNames, nil
}
