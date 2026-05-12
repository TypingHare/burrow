package share

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
	mapset "github.com/deckarep/golang-set/v2"
)

// GetAllCartonNames returns names of all cartons registered in the warehouse,
// sorted alphabetically.
func GetAllCartonNames(warehouse *kernel.Warehouse) []string {
	return slices.Sorted(maps.Keys(warehouse.CartonsByNames))
}

// GetCartonNames returns carton names used in the chamber, sorted
// alphabetically.
func GetCartonNames(chamber *kernel.Chamber) ([]string, error) {
	cartonNameSet := mapset.NewSet[string]()
	decorIDs := slices.Collect(maps.Keys(chamber.Renovator.DecorsByIDs))
	for _, decorID := range decorIDs {
		_, cartonName, err := kernel.SplitDecorID(decorID)
		if err != nil {
			return nil, err
		}

		cartonNameSet.Add(cartonName)
	}

	cartonNames := cartonNameSet.ToSlice()
	slices.Sort(cartonNames)

	return cartonNames, nil
}
