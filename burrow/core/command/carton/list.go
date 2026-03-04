package carton

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(chamber *kernel.Chamber) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show cartons used in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			// TODO: also print the versions
			if all {
				allCartonNames := share.GetAllCartonNames(
					chamber.Burrow().Warehouse(),
				)
				for _, cartonName := range allCartonNames {
					cmd.Println(cartonName)
				}

				return nil
			}

			cartonNames, err := share.GetCartonNames(chamber)
			if err != nil {
				return err
			}
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
