package decoration

import (
	"fmt"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func RemoveCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "remove <decoration>",
		Short: "Remove a decoration from the chamber",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			decorationID := args[0]

			directDependencies := d.Spec().DirectDependencies
			if !slices.Contains(directDependencies, decorationID) {
				return fmt.Errorf(
					"Cannot remove decoration %q because it is not a direct"+
						"dependency of the core decoration",
					decorationID,
				)
			}

			return share.UpdateBlueprintAndRedig(
				d.Chamber(),
				func(blueprint kernel.Blueprint) error {
					delete(blueprint, decorationID)
					return nil
				},
			)
		},
	}

	return command
}
