package carton

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/deckarep/golang-set/v2"
	"github.com/spf13/cobra"
)

func ListCommand(chamber *kernel.Chamber) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show cartons used in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			if all {
				cartonNames := maps.Keys(
					chamber.Burrow().Warehouse().CartonMap(),
				)
				for _, cartonName := range slices.Sorted(cartonNames) {
					cmd.Println(cartonName)
				}

				return nil
			}

			cartonNameSet := mapset.NewSet[string]()
			decorationIDs := chamber.Renovator().OrderedDecorationIDs()
			for _, decorationID := range decorationIDs {
				_, cartonName, err := chamber.Burrow().
					Warehouse().
					SplitDecorationID(decorationID)
				if err != nil {
					return err
				}

				cartonNameSet.Add(cartonName)
			}

			cartonNames := cartonNameSet.ToSlice()
			slices.Sort(cartonNames)
			for _, cartonName := range cartonNames {
				cmd.Println(cartonName)
			}

			return nil
		},
	}

	command.Flags().BoolVarP(&all, "all", "a", false,
		"Show all cartons in the warehouse",
	)

	return command
}
