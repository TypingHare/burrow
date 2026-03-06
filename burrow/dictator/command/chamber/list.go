package chamber

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func ListCommand(d share.DictatorDecorationLike) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "List dug chambers",
		RunE: func(cmd *cobra.Command, args []string) error {
			burrow := d.Chamber().Burrow()

			if all {
				allChamberNames, err := share.GetAllChamberNames(burrow)
				if err != nil {
					return fmt.Errorf(
						"failed to get all chamber names: %w",
						err,
					)
				}

				for _, chamberName := range allChamberNames {
					cmd.Println(chamberName)
				}
			}

			for _, chamberName := range share.GetDugChamberNames(burrow) {
				cmd.Println(chamberName)
			}

			return nil
		},
	}

	command.Flags().BoolVarP(
		&all, "all", "a", false,
		"List all chambers, including those not dug yet",
	)

	return command
}
