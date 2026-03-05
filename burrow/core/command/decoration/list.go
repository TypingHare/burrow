package decoration

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func ListCommand(d share.CoreDecorationLike) *cobra.Command {
	var all bool
	var root bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show all decorations in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			if all {
				decorationIDs := maps.Keys(
					d.Chamber().Burrow().Warehouse().DecorationFactoryMap(),
				)
				for _, decorationID := range slices.Sorted(decorationIDs) {
					cmd.Println(decorationID)
				}

				return nil
			}

			if root {
				rootDecorationIDs := share.GetRootDecorationIDs(
					d.Chamber().Renovator(),
				)
				for _, rootDecorationID := range rootDecorationIDs {
					cmd.Println(rootDecorationID)
				}

				return nil
			}

			deocrationIDs := share.GetSortedDecorationIDs(d.Chamber())
			for _, decorationID := range deocrationIDs {
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
