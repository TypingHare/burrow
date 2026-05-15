package decor

import (
	"fmt"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func AddCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "add <decor>",
		Short: "Add a decor to the chamber",
		Long: strings.TrimSpace(`
This command adds a decor to the chamber.
        `),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			decorID := args[0]
			if slices.Contains(decor.DirectDependencies(), decorID) {
				return fmt.Errorf("decor %q already exists", decorID)
			}

			_, err := share.UpdateBlueprintAndReCreate(
				decor.Chamber(),
				func(blueprint kernel.Blueprint) error {
					decor.SetDirectDependencies(
						append(decor.DirectDependencies(), decorID),
					)

					decor.UpdateSpec()

					return nil
				},
			)
			if err != nil {
				return fmt.Errorf("failed to add decor %q: %w", decorID, err)
			}

			return nil
		},
	}

	return command
}
