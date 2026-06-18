package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/command/blueprint"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func BlueprintCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "blueprint",
		Short: "Manage blueprint of the chamber",
		Long: strings.TrimSpace(`
This command group works with the current chamber's blueprint.

A Blueprint is a collection of decor specs that Burrow uses to determine how to
set up a chamber. It maps decor IDs to their corresponding specs, each of which 
is a set of string key-value pairs that describe the properties of the decor.

Use these commands to inspect the blueprint of the current chamber or save it
to disk.
		`),
		Args: cobra.NoArgs,
	}

	command.AddCommand(blueprint.SaveCommand(decor))
	command.AddCommand(blueprint.ShowCommand(decor))
	command.AddCommand(blueprint.PathCommand(decor))

	return command
}
