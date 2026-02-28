package core

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/command"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

type CoreDecoration struct {
	kernel.Decoration[CoreSpec]

	RootCommand *cobra.Command
}

func (d *CoreDecoration) SpecAny() any           { return d.Spec() }
func (d *CoreDecoration) Dependencies() []string { return []string{} }

func (d *CoreDecoration) Assemble() error {
	// Stop Cobra from printing usage or errors automatically.
	d.RootCommand.SilenceUsage = true
	d.RootCommand.SilenceErrors = true

	// Set the chamber's handler to the root command.
	d.Chamber().Handler = CoreHandler

	d.AddCommand(command.CartonCommand)
	d.AddCommand(command.DecorationCommand)

	return nil
}

func (d *CoreDecoration) Launch() error      { return nil }
func (d *CoreDecoration) Terminate() error   { return nil }
func (d *CoreDecoration) Disassemble() error { return nil }

func BuildCoreDecoration(
	chamber *kernel.Chamber,
	spec CoreSpec,
) (kernel.DecorationInstance, error) {
	return &CoreDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
		RootCommand: &cobra.Command{
			Use: chamber.Name(),
			RunE: func(cmd *cobra.Command, args []string) error {
				if len(args) > 0 {
					return fmt.Errorf(
						"unknown command %q for %q",
						args[0],
						cmd.CommandPath(),
					)
				}

				return nil
			},
		},
	}, nil
}

// AddCommand adds a command to the root command of the core decoration. The
// commandFactory is a function that takes a chamber and returns a Cobra
// command.
func (d *CoreDecoration) AddCommand(
	commandFactory func(*kernel.Chamber) *cobra.Command,
) {
	d.RootCommand.AddCommand(commandFactory(d.Chamber()))
}
