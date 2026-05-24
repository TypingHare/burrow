// Package core provides Burrow's built-in core decor.
package core

import (
	"fmt"
	"reflect"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/command"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

const DirectDependenciesSeparator = ":"

const (
	SpecKeyDirectDependencies = "direct_dependencies"
)

type Decor struct {
	*kernel.Decor

	// rootCommand is the root of the chamber command tree.
	rootCommand *cobra.Command

	// directDependencies lists the IDs of decors that are directly installed by
	// users.
	directDependencies []string
}

func (d *Decor) UpdateSpec() error {
	d.Spec().Set(
		SpecKeyDirectDependencies,
		strings.Join(d.directDependencies, DirectDependenciesSeparator),
	)
	return nil
}

// RootCommand returns the root command of the decor.
func (d *Decor) RootCommand() *cobra.Command {
	return d.rootCommand
}

// DirectDependencies returns the IDs of decors that are directly installed by
// users.
func (d *Decor) DirectDependencies() []string {
	return d.directDependencies
}

// SetDirectDependencies updates the list of direct dependencies for the
// decor.
func (d *Decor) SetDirectDependencies(directDependencies []string) {
	d.directDependencies = directDependencies
}

// RegisterToCarton registers the core decor definition with carton.
func RegisterToCarton(carton *kernel.Carton) error {
	return CreateAndAddDecorDefToCarton(
		carton,
		"core",
		func(chamber *kernel.Chamber, spec kernel.Vars) (*Decor, error) {
			directDependenciesString := spec.Get(SpecKeyDirectDependencies)
			directDependencies := strings.Split(directDependenciesString, ":")
			if directDependenciesString == "" {
				directDependencies = []string{
					kernel.GetDecorID("core", kernel.CartonName),
				}
			}

			return &Decor{
				Decor: kernel.NewDecor(chamber, spec),
				rootCommand: GetDefaultRootCommand(
					chamber.Name,
					kernel.Version,
				),
				directDependencies: directDependencies,
			}, nil
		},
		func(chamber *kernel.Chamber, decor *Decor) error {
			decor.AssembleFunc = func() error {
				chamber.CommandHandler = GetCoreCommandHandler(decor)

				// Register commands.
				decor.SetCommand(nil, command.EnvCommand(decor))
				decor.SetCommand(nil, command.BlueprintCommand(decor))
				decor.SetCommand(nil, command.CartonCommand(decor))
				decor.SetCommand(nil, command.DecorCommand(decor))

				return nil
			}

			return nil
		},
	)
}

// GetCoreCommandHandler returns a CommandHandler based on the rootCommand of d.
func GetCoreCommandHandler(
	d *Decor,
) func(*kernel.Chamber, []string) (int, error) {
	return func(chamber *kernel.Chamber, args []string) (int, error) {
		if chamber == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("chamber is nil")
		}

		rootCommand := d.rootCommand
		if rootCommand == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("root command is nil")
		}

		rootCommand.SetArgs(args)
		if err := rootCommand.Execute(); err != nil {
			return kernel.GeneralError, chamber.Error(
				"error executing the command",
				err,
			)
		}

		return kernel.Success, nil
	}
}

// UseDecor returns the core Decor installed in chamber.
func UseDecor(chamber *kernel.Chamber) (*Decor, error) {
	decor, err := chamber.Renovator.GetDecorByType(reflect.TypeFor[*Decor]())
	if err != nil {
		return nil, fmt.Errorf("failed to get core decor: %w", err)
	}

	return decor.(*Decor), nil
}
