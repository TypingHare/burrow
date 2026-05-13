package decor

import (
	"fmt"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func RemoveCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "remove <decor>",
		Short: "Remove a decor from the chamber",
		Long: strings.TrimSpace(`
This command removes a decor from the current chamber.

It only removes decors that are direct dependencies. After removal, Burrow
re-creates the chamber so the running chamber state matches the new blueprint.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			decorID := args[0]
			_ = decorID

			if decorID == kernel.GetDecorID("core", kernel.CartonName) {
				return fmt.Errorf(
					"Cannot remove decor %q because it is reserved.",
					decorID,
				)
			}

			directDependencies := decor.DirectDependencies()
			if !slices.Contains(directDependencies, decorID) {
				return fmt.Errorf(
					"Cannot remove decor %q because it is not a direct "+
						"dependency.",
					decorID,
				)
			}

			_, err := share.UpdateBlueprintAndReCreate(
				decor.Chamber(),
				func(blueprint kernel.Blueprint) error {
					newDirectDependencies := make(
						[]string,
						0,
						max(0, len(directDependencies)-1),
					)
					for _, dep := range directDependencies {
						if dep != decorID {
							newDirectDependencies = append(
								newDirectDependencies,
								dep,
							)
						}
					}

					decor.UpdateDirectDependencies(newDirectDependencies)
					decor.UpdateSpec()
					delete(blueprint, decorID)

					return nil
				},
			)
			if err != nil {
				return fmt.Errorf("failed to remove decor: %w", err)
			}

			return nil
		},
	}

	return command
}
