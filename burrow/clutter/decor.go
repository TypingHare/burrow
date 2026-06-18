package clutter

import (
	"fmt"
	"reflect"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/command"
	c "github.com/TypingHare/burrow/v2026/burrow/clutter/command/carton"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/kernel"
)

const (
	CartonNamePathSeparator = "="
	CartonStringsSeparator  = ":"
)

const (
	SpecKeyCartons = "cartons"
)

type Decor struct {
	*kernel.Decor
	cartonDefs []*share.CartonDef
}

func (d *Decor) Dependencies() []string {
	return []string{
		kernel.GetDecorID("core", kernel.CartonName),
	}
}

func (d *Decor) UpdateSpec() error {
	cartonStrings := make([]string, len(d.cartonDefs))
	for i, carton := range d.cartonDefs {
		cartonStrings[i] = carton.Name
		if carton.Path != "" {
			cartonStrings[i] += CartonNamePathSeparator + carton.Path
		}
	}

	d.Spec().Set(
		SpecKeyCartons,
		strings.Join(cartonStrings, CartonStringsSeparator),
	)

	return nil
}

// CartonDefs returns the list of carton definitions for the decor.
func (d *Decor) CartonDefs() []*share.CartonDef {
	return d.cartonDefs
}

// SetCartonDefs sets the list of carton definitions for the decor.
func (d *Decor) SetCartonDefs(cartons []*share.CartonDef) {
	d.cartonDefs = cartons
}

func RegisterToCarton(carton *kernel.Carton) error {
	return core.CreateAndAddDecorDefToCarton(
		carton,
		"clutter",
		func(chamber *kernel.Chamber, spec kernel.Vars) (*Decor, error) {
			cartonStrings := strings.Split(
				spec.Get(SpecKeyCartons),
				CartonStringsSeparator,
			)
			cartons, err := getCartonDefsByCartonStrings(cartonStrings)
			if err != nil {
				return nil, err
			}

			return &Decor{
				Decor:      kernel.NewDecor(chamber, spec),
				cartonDefs: cartons,
			}, nil
		},
		func(chamber *kernel.Chamber, decor *Decor) error {
			decor.AssembleFunc = func() error {
				// Register commands.
				coreDecor, err := core.UseDecor(chamber)
				if err != nil {
					return fmt.Errorf("failed to use core decor: %w", err)
				}

				err = coreDecor.SetCommand([]string{"carton"},
					c.InstallCommand(decor),
					c.UninstallCommand(decor),
				)
				if err != nil {
					return chamber.Error("failed to set commands: %w", err)
				}

				err = coreDecor.SetCommand(nil, command.BurrowCommand(decor))
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
		return nil, fmt.Errorf("failed to get clutter decor: %w", err)
	}

	return decor.(*Decor), nil
}

// getCartonDefsByCartonStrings parses a list of carton strings into a list of
// CartonDefs. Each carton string is expected to be in the format "name=path".
// If the list contains a single empty string, it returns a default carton
// definition based on the kernel version.
func getCartonDefsByCartonStrings(
	cartonStrings []string,
) ([]*share.CartonDef, error) {
	if len(cartonStrings) == 1 && cartonStrings[0] == "" {
		return []*share.CartonDef{{Name: kernel.CartonName}}, nil
	}

	cartonDefs := make([]*share.CartonDef, len(cartonStrings))
	for i, cartonString := range cartonStrings {
		if strings.Contains(cartonString, CartonNamePathSeparator) {
			parts := strings.SplitN(
				cartonString,
				CartonNamePathSeparator,
				2,
			)
			cartonDefs[i] = &share.CartonDef{
				Name: parts[0],
				Path: parts[1],
			}
		} else {
			cartonDefs[i] = &share.CartonDef{
				Name: cartonString,
			}
		}
	}

	return cartonDefs, nil
}
