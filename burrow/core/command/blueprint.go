package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/command/blueprint"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func BlueprintCommand(chamber *kernel.Chamber) *cobra.Command {
	var showJson bool

	command := &cobra.Command{
		Use:   "blueprint",
		Short: "Manage blueprint of the chamber",
	}

	command.AddCommand(blueprint.ShowCommand(chamber))
	command.AddCommand(blueprint.UpdateCommand(chamber))

	command.Flags().BoolVarP(
		&showJson, "json", "j", false,
		"Output the blueprint in JSON format",
	)

	return command
}
