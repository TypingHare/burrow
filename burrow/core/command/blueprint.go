package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/command/blueprint"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func BlueprintCommand(d share.CoreDecorationLike) *cobra.Command {
	var showJson bool

	command := &cobra.Command{
		Use:   "blueprint",
		Short: "Manage blueprint of the chamber",
		Long: strings.TrimSpace(`
This command group works with the current chamber's blueprint.

A blueprint is the saved configuration that tells Burrow which
decorations belong to the chamber and how those decorations are
configured. Use these commands to inspect it, refresh it from the
installed decorations, or save it to disk.
		`),
	}

	command.AddCommand(blueprint.SaveCommand(d))
	command.AddCommand(blueprint.ShowCommand(d))
	command.AddCommand(blueprint.UpdateCommand(d))

	command.Flags().BoolVarP(
		&showJson, "json", "j", false,
		"Output the blueprint in JSON format",
	)

	return command
}
