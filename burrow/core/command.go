package core

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

// GetDefaultRootCommand returns the default Cobra root command for a chamber.
// The root command itself performs no action and reports unknown subcommands as
// errors.
func GetDefaultRootCommand(chamberName string, version string) *cobra.Command {
	command := &cobra.Command{
		Use:           os.Args[0] + " " + chamberName,
		Version:       version,
		SilenceErrors: true,
		SilenceUsage:  true,
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
	}

	command.SetVersionTemplate("{{.Version}}\n")
	return command
}

// GetCommand resolves a command by path from RootCommand.
// An empty path returns RootCommand itself.
func (d *Decor) GetCommand(path []string) (*cobra.Command, error) {
	if len(path) == 0 {
		return d.rootCommand, nil
	}

	currentCommand := d.rootCommand
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
func (d *Decor) MergeCommand(
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
	if err := d.MergeCommandTree(leftCommand, rightCommand); err != nil {
		return fmt.Errorf("failed to merge command tree: %w", err)
	}

	return nil
}

// mergeCommandTree recursively merges subcommands from rightCommand into
// leftCommand by command name.
func (d *Decor) MergeCommandTree(
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

		if err := d.MergeCommandTree(
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
func (d *Decor) CreateParentCommand(
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
func (d *Decor) SetCommand(
	path []string,
	commands ...*cobra.Command,
) error {
	err := d.MergeCommand(path, d.CreateParentCommand(commands...))
	if err != nil {
		return fmt.Errorf("failed to merge commands: %w", err)
	}

	return nil
}
