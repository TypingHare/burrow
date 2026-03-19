package burrow

import (
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/kernel"
)

func RegisterCartonToWarehouse(warehouse *kernel.Warehouse) error {
	carton := kernel.NewCarton(kernel.CartonName, kernel.Version)

	if err := core.RegisterToCarton(carton); err != nil {
		return err
	}

	return nil
}
