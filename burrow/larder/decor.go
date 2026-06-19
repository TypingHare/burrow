package larder

import (
	"fmt"
	"reflect"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/kernel"
)

const DecorName = "larder"

type Decor struct {
	*kernel.Decor

	// cabinetsByNames stores named cabinets registered by other decors.
	cabinetsByNames map[string]any
}

func (d *Decor) Dependencies() []string {
	return []string{
		kernel.GetDecorID(core.DecorName, kernel.CartonName),
	}
}

// CabinetsByNames returns the mutable registry of named cabinets for this
// chamber.
func (d *Decor) CabinetsByNames() map[string]any {
	return d.cabinetsByNames
}

func RegisterToCarton(carton *kernel.Carton) error {
	return core.CreateAndAddDecorDefToCarton(
		carton,
		DecorName,
		func(chamber *kernel.Chamber, spec kernel.Vars) (*Decor, error) {
			return &Decor{
				Decor:           kernel.NewDecor(chamber, spec),
				cabinetsByNames: make(map[string]any),
			}, nil
		},
		func(chamber *kernel.Chamber, decor *Decor) error {
			return nil
		},
	)
}

func UseDecor(chamber *kernel.Chamber) (*Decor, error) {
	decor, err := chamber.Renovator.GetDecorByType(reflect.TypeFor[*Decor]())
	if err != nil {
		return nil, fmt.Errorf(
			"failed to get the %q decor: %w",
			DecorName,
			err,
		)
	}

	return decor.(*Decor), nil
}
