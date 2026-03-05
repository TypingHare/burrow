package chamber

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func ListCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "list",
		Short: "List dug chambers",
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberMap := d.Chamber().Burrow().Architect().ChamberMap()
			for _, chamberName := range slices.Sorted(maps.Keys(chamberMap)) {
				cmd.Println(chamberName)
			}

			return nil
		},
	}

	return command
}
