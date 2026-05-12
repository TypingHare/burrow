// Package core provides Burrow's built-in core decor.
package core

import (
	"fmt"
	"reflect"

	"github.com/TypingHare/burrow/v2026/burrow/core/command"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// Decor provides the core command tree for a chamber.
type Decor struct {
	*kernel.Decor

	// RootCommand is the root of the chamber command tree.
	RootCommand *cobra.Command
}

// GetRootCommand returns the root command of the decor.
func (d *Decor) GetRootCommand() *cobra.Command {
	return d.RootCommand
}

// RegisterToCarton registers the core decor definition with carton.
func RegisterToCarton(carton *kernel.Carton) error {
	return CreateAndAddDecorDefToCarton(
		carton,
		"core",
		func(chamber *kernel.Chamber, spec kernel.Vars) (*Decor, error) {
			return &Decor{
				Decor: kernel.NewDecor(chamber, spec),
				RootCommand: GetDefaultRootCommand(
					chamber.Name,
					kernel.Version,
				),
			}, nil
		},
		func(chamber *kernel.Chamber, decor *Decor) error {
			decor.AssembleFunc = func() error {
				chamber.CommandHandler = GetCoreCommandHandler(decor)

				// Register commands.
				decor.SetCommand(nil, command.EnvCommand(decor))
				decor.SetCommand(nil, command.BlueprintCommand(decor))

				return nil
			}

			return nil
		},
	)
}

// GetCoreCommandHandler returns a CommandHandler backed by d's root command.
func GetCoreCommandHandler(
	d *Decor,
) func(*kernel.Chamber, []string) (int, error) {
	return func(chamber *kernel.Chamber, args []string) (int, error) {
		if chamber == nil {
			return kernel.ErrorNullPointer, fmt.Errorf("chamber is nil")
		}

		rootCommand := d.RootCommand
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
func UseDecor(chamber *kernel.Chamber) (kernel.IDecor, error) {
	return chamber.Renovator.GetDecorByType(reflect.TypeFor[*Decor]())
}
