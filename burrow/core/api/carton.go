package api

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetCartonNames retrieves the names of cartons in the specified chamber. If
// all is true, it retrieves names of all cartons in the warehouse.
func GetCartonNames(chamber *kernel.Chamber, all bool) ([]string, error) {
	if all {
		return share.GetAllCartonNames(chamber.Burrow.Warehouse), nil
	}

	cartonNames, err := share.GetCartonNames(chamber)
	if err != nil {
		return nil, fmt.Errorf("failed to get carton names: %w", err)
	}

	return cartonNames, nil
}
