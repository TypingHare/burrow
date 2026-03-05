package chamber

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func BuryCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "bury <chamber>",
		Short: "Bury a chamber",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := args[0]
			err := d.Chamber().Burrow().Architect().Bury(chamberName)
			if err != nil {
				return fmt.Errorf("failed to bury chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
