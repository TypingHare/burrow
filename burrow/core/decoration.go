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

func (d *CoreDecoration) GetCommand(path []string) (*cobra.Command, error) {
	if len(path) == 0 {
		return d.RootCommand, nil
	}

	currentCommand := d.RootCommand
	for _, next := range path {
		found := false
		for _, command := range currentCommand.Commands() {
			if command.Name() == next {
				currentCommand = command
				found = true
				break
			}
		}

		if !found {
			return nil, fmt.Errorf(
				"command %q not found in path %q",
				next,
				path,
			)
		}
	}

	return currentCommand, nil
}

// InsertCommand inserts a subcommand at the specified path in the command tree
// of the core decoration. The path is a slice of command names that specifies
// where to insert the command.
func (d *CoreDecoration) InsertCommand(
	path []string,
	command *cobra.Command,
) error {
	targetCommand, err := d.GetCommand(path)
	if err != nil {
		return fmt.Errorf(
			"failed to insert command at path %q: %w",
			path,
			err,
		)
	}

	targetCommand.AddCommand(command)
	return nil
}

func (d *CoreDecoration) MergeCommand(
	path []string,
	command *cobra.Command,
) error {
	// TODO: This would be hard. But after we implement this, we can get rid of
	// InsertCommand and AddCommand.
	return nil
}
