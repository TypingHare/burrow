package decoration

import (
	"fmt"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func AddCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "add <decoration>",
		Short: "Add a decoration to the chamber",
		Long: strings.TrimSpace(`
This command adds a decoation to the chamber.
        `),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			decorationID := args[0]
			if slices.Contains(d.Spec().DirectDependencies, decorationID) {
				return fmt.Errorf(
					"decoration '%s' already exists",
					decorationID,
				)
			}

			_, err := share.UpdateBlueprintAndRedig(
				d.Chamber(),
				func(blueprint kernel.Blueprint) error {
					spec := d.Spec()
					spec.DirectDependencies = append(
						spec.DirectDependencies,
						decorationID,
					)

					blueprint["core@"+kernel.CartonName] = d.RawSpec()
					blueprint[decorationID] = kernel.NewRawSpec()
					return nil
				},
			)
			if err != nil {
				return fmt.Errorf("failed to add decoration: %w", err)
			}

			return nil
		},
	}

	return command
}
