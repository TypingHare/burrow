package dictator

import (
	"fmt"
	"reflect"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command/blueprint"
	"github.com/TypingHare/burrow/v2026/kernel"
)

const (
	DecorName = "dictator"
)

type Decor struct {
	*kernel.Decor
}

func (d *Decor) Dependencies() []string {
	return []string{
		kernel.GetDecorID(core.DecorName, kernel.CartonName),
	}
}

func RegisterToCarton(carton *kernel.Carton) error {
	return core.CreateAndAddDecorDefToCarton(
		carton,
		DecorName,
		func(chamber *kernel.Chamber, spec kernel.Vars) (*Decor, error) {
			return &Decor{
				Decor: kernel.NewDecor(chamber, spec),
			}, nil
		},
		func(chamber *kernel.Chamber, decor *Decor) error {
			decor.AssembleFunc = func() error {
				// Register commands.
				coreDecor, err := core.UseDecor(chamber)
				if err != nil {
					return err
				}

				err = coreDecor.SetCommand(
					nil,
					command.ChamberCommand(decor),
				)
				if err != nil {
					return chamber.Error("failed to set commands: %w", err)
				}

				err = coreDecor.SetCommand(
					[]string{"blueprint"},
					blueprint.CreateCommand(decor),
					blueprint.DeleteCommand(decor),
				)
				if err != nil {
					return chamber.Error("failed to set commands: %w", err)
				}

				return nil
			}

			return nil
		},
	)
}

func UseDecor(chamber *kernel.Chamber) (*Decor, error) {
	decor, err := chamber.Renovator.GetDecorByType(reflect.TypeFor[*Decor]())
	if err != nil {
		return nil, fmt.Errorf("failed to get the %q decor: %w", DecorName, err)
	}

	return decor.(*Decor), nil
}
