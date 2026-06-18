// Package burrow registers Burrow's built-in carton.
package burrow

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter"
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// RegisterCartonToWarehouse registers the Burrow carton to the Warehouse.
func RegisterCartonToWarehouse(warehouse *kernel.Warehouse) error {
	carton := kernel.NewCarton(kernel.CartonName, kernel.Version)

	if err := core.RegisterToCarton(carton); err != nil {
		return err
	}

	if err := clutter.RegisterToCarton(carton); err != nil {
		return err
	}

	return warehouse.RegisterCarton(carton)
}
