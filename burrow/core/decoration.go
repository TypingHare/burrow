package core

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/burrow/core/command"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// CoreDecoration is the foundational decoration that wires a chamber to Cobra
// and hosts the chamber's top-level command tree.
type CoreDecoration struct {
	// Decoration carries the typed core spec and chamber reference.
	kernel.Decoration[share.CoreSpec]

	// RootCommand is the root of the chamber command tree.
	RootCommand *cobra.Command
}

// Dependencies declares the core decoration's direct runtime dependencies.
// Core sits at the base of the command system, so it has none.
func (d *CoreDecoration) Dependencies() []string { return []string{} }

// RawSpec serializes the current core spec into the unstructured format used
// by chamber blueprints.
func (d *CoreDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"directDependencies": d.Spec().DirectDependencies,
	}
}

// GetRootCommand returns the root Cobra command of the core decoration, which
// serves as the entry point for all command execution in the chamber.
func (d *CoreDecoration) GetRootCommand() *cobra.Command {
	return d.RootCommand
}

// Assemble installs the core command surface into the chamber by setting the
// chamber handler and registering built-in top-level commands.
func (d *CoreDecoration) Assemble() error {
	// Stop Cobra from printing usage or errors automatically.
	d.RootCommand.SilenceUsage = true
	d.RootCommand.SilenceErrors = true

	// Set the chamber's handler to the root command.
	d.Chamber().Handler = share.GetCoreHandler(d)

	d.SetCommand(nil, command.RedigCommand(d))
	d.SetCommand(nil, command.CartonCommand(d))
	d.SetCommand(nil, command.DecorationCommand(d))
	d.SetCommand(nil, command.BlueprintCommand(d))

	return nil
}

// Launch starts runtime behavior after assembly. Core has no launch step.
func (d *CoreDecoration) Launch() error { return nil }

// Terminate stops runtime behavior before disassembly. Core has no terminate
// step.
func (d *CoreDecoration) Terminate() error { return nil }

// Disassemble releases resources created during assembly. Core has no
// disassembly step.
func (d *CoreDecoration) Disassemble() error { return nil }

// BuildCoreDecoration constructs the core decoration instance and initializes
// the root Cobra command for a chamber.
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

// UseDecoration resolves the core decoration from the same chamber as d.
// It returns an error when the chamber does not contain a compatible core
// decoration instance.
func UseDecoration(d kernel.DecorationInstance) (*CoreDecoration, error) {
	return kernel.Use[*CoreDecoration](d.Chamber())
}

// GetCommand resolves a command by path from RootCommand.
// An empty path returns RootCommand itself.
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

// MergeCommand merges rightCommand into the command node identified by path.
// If path does not exist but its parent exists, rightCommand is attached to the
// parent as a new subcommand.
func (d *CoreDecoration) MergeCommand(
	path []string,
	rightCommand *cobra.Command,
) error {
	if rightCommand == nil {
		return fmt.Errorf("right command cannot be nil")
	}

	leftCommand, err := d.GetCommand(path)
	if err != nil && len(path) > 0 {
		subPath := path[:len(path)-1]
		leftParentCommand, parentErr := d.GetCommand(subPath)
		if parentErr != nil {
			return fmt.Errorf(
				"failed to get parent command in path %q: %w",
				subPath,
				parentErr,
			)
		}

		// If the left command does not exist, add the right command as a new
		// subcommand to the parent command.
		leftParentCommand.AddCommand(rightCommand)
		return nil
	} else if err != nil {
		return fmt.Errorf("failed to get command in path %q: %w", path, err)
	}

	// Merge the right command into the left command by adding all subcommands
	// of the right command to the left command recursively.
	if err := d.mergeCommandTree(leftCommand, rightCommand); err != nil {
		return fmt.Errorf("failed to merge command tree: %w", err)
	}

	return nil
}

// mergeCommandTree recursively merges subcommands from rightCommand into
// leftCommand by command name.
func (d *CoreDecoration) mergeCommandTree(
	leftCommand *cobra.Command,
	rightCommand *cobra.Command,
) error {
	if leftCommand == nil || rightCommand == nil {
		return fmt.Errorf("left and right commands cannot be nil")
	}

	rightSubCommands := rightCommand.Commands()
	for _, rightSubCommand := range rightSubCommands {
		var leftSubCommand *cobra.Command
		for _, candidate := range leftCommand.Commands() {
			if candidate.Name() == rightSubCommand.Name() {
				leftSubCommand = candidate
				break
			}
		}

		if leftSubCommand == nil {
			leftCommand.AddCommand(rightSubCommand)
			continue
		}

		if err := d.mergeCommandTree(
			leftSubCommand,
			rightSubCommand,
		); err != nil {
			return err
		}
	}

	return nil
}

// CreateParentCommand creates an anonymous parent node that contains the given
// subcommands. It is used as a merge carrier by SetCommand.
func (d *CoreDecoration) CreateParentCommand(
	subcommands ...*cobra.Command,
) *cobra.Command {
	command := &cobra.Command{}

	for _, subcommand := range subcommands {
		command.AddCommand(subcommand)
	}

	return command
}

// SetCommand inserts or merges commands at the command node identified by path.
// Existing nodes are merged by name; missing nodes are added under the nearest
// existing parent.
func (d *CoreDecoration) SetCommand(
	path []string,
	commands ...*cobra.Command,
) error {
	err := d.MergeCommand(path, d.CreateParentCommand(commands...))
	if err != nil {
		return fmt.Errorf("failed to merge commands: %w", err)
	}

	return nil
}
