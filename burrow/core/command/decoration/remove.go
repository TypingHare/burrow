package decoration

import (
	"fmt"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func RemoveCommand(
	chamber *kernel.Chamber,
	coreDecoration share.CoreDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "remove <decoration-id>",
		Short: "Remove a decoration from the chamber",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			decorationID := args[0]

			directDependencies := coreDecoration.Spec().DirectDependencies
			exists := slices.Contains(directDependencies, decorationID)
			if exists {
				return chamber.Error(
					fmt.Sprintf(
						"Cannot remove decoration %q because it is a direct "+
							"dependency of the core decoration",
						decorationID,
					),
					nil,
				)
			}

			return share.UpdateBlueprintAndRedig(
				chamber,
				func(blueprint kernel.Blueprint) error {
					delete(chamber.Blueprint(), decorationID)
					return nil
				},
			)
		},
	}

	return command
}
