package carton

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(chamber *kernel.Chamber) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show cartons used in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			if all {
				for _, cartonName := range slices.Sorted(
					maps.Keys(chamber.Burrow().Warehouse().CartonMap()),
				) {
					cmd.Println(cartonName)
				}
				return nil
			}

			cartonNames := make(map[string]struct{})
			for decorationID := range chamber.Renovator().Decorations() {
				_, cartonName, err := kernel.SplitDecorationID(decorationID)
				if err != nil {
					return err
				}

				cartonNames[cartonName] = struct{}{}
			}

			for _, cartonName := range slices.Sorted(maps.Keys(cartonNames)) {
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
