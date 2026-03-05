package chamber

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func DigCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "dig <chamber>",
		Short: "Dig a chamber",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := args[0]
			_, err := d.Chamber().Burrow().Architect().Dig(chamberName)
			if err != nil {
				return fmt.Errorf("failed to dig chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
