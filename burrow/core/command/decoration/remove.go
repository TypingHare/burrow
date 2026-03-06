package decoration

import (
	"fmt"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// RemoveCommand builds the `decoration remove` command.
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
					"Cannot remove decoration %q because it is not a direct "+
						"dependency of the core decoration",
					decorationID,
				)
			}

			_, err := share.UpdateBlueprintAndRedig(
				d.Chamber(),
				func(blueprint kernel.Blueprint) error {
					spec := d.Spec()
					newDirectDependencies := make(
						[]string,
						0,
						len(spec.DirectDependencies)-1,
					)
					for _, dep := range spec.DirectDependencies {
						if dep != decorationID {
							newDirectDependencies = append(
								newDirectDependencies,
								dep,
							)
						}
					}
					spec.DirectDependencies = newDirectDependencies

					delete(blueprint, decorationID)
					blueprint["core@"+kernel.CartonName] = d.RawSpec()

					return nil
				},
			)
			if err != nil {
				return fmt.Errorf("failed to remove decoration: %w", err)
			}

			return nil
		},
	}

	return command
}
