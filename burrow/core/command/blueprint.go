package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/command/blueprint"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// BlueprintCommand builds the `blueprint` command group for chamber blueprint
// inspection and maintenance.
func BlueprintCommand(d share.CoreDecorationLike) *cobra.Command {
	var showJson bool

	command := &cobra.Command{
		Use:   "blueprint",
		Short: "Manage blueprint of the chamber",
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
