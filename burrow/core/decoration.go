package core

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/core/command"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

type CoreDecoration struct {
	kernel.Decoration[share.CoreSpec]
	RootCommand *cobra.Command
}

func (d *CoreDecoration) Dependencies() []string { return []string{} }

func (d *CoreDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"DirectDependencies": d.Spec().DirectDependencies,
	}
}

func (d *CoreDecoration) Assemble() error {
	// Stop Cobra from printing usage or errors automatically.
	d.RootCommand.SilenceUsage = true
	d.RootCommand.SilenceErrors = true

	// Set the chamber's handler to the root command.
	d.Chamber().Handler = CoreHandler

	d.AddCommand(command.RedigCommand(d.Chamber()))
	d.AddCommand(command.CartonCommand(d.Chamber()))
	d.AddCommand(command.DecorationCommand(d.Chamber(), d))

	return nil
}

func (d *CoreDecoration) Launch() error      { return nil }
func (d *CoreDecoration) Terminate() error   { return nil }
func (d *CoreDecoration) Disassemble() error { return nil }

func (d *CoreDecoration) Command() *cobra.Command {
	return d.RootCommand
}

func BuildCoreDecoration(
	chamber *kernel.Chamber,
	spec share.CoreSpec,
) (kernel.DecorationInstance, error) {
	return &CoreDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
		RootCommand: &cobra.Command{
			Use: os.Args[0] + " " + chamber.Name(),
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

// AddCommand adds a subcommand to the root command of the core decoration.
func (d *CoreDecoration) AddCommand(command *cobra.Command) {
	d.RootCommand.AddCommand(command)
}
