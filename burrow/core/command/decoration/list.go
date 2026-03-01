package decoration

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(chamber *kernel.Chamber) *cobra.Command {
	var all bool
	var root bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show decorations in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			if all {
				decorationIDs := maps.Keys(
					chamber.Burrow().Warehouse().DecorationFactoryMap(),
				)
				for _, cartonName := range slices.Sorted(decorationIDs) {
					cmd.Println(cartonName)
				}

				return nil
			}

			if root {
				rootDecorationIDs := share.GetRootDecorationIDs(
					chamber.Renovator(),
				)
				for _, rootDecorationID := range rootDecorationIDs {
					cmd.Println(rootDecorationID)
				}

				return nil
			}

			decorationIDs := chamber.Renovator().OrderedDecorationIDs()
			for _, decorationID := range decorationIDs {
				cmd.Println(decorationID)
			}

			return nil
		},
	}

	command.Flags().BoolVarP(&all, "all", "a", false,
		"Show all decorations registered in the warehouse",
	)

	command.Flags().BoolVarP(&root, "root", "r", false,
		"Only show root decorations",
	)

	return command
}
