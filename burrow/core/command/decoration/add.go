package decoration

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func AddCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "add <decoration-id>",
		Short: "Add a decoration to the chamber",
		Long: strings.TrimSpace(`
This command simplify adds.
        `),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			decorationID := args[0]

			return share.UpdateBlueprintAndRedig(
				chamber,
				func(blueprint kernel.Blueprint) error {
					blueprint[decorationID] = kernel.NewRawSpec()
					return nil
				},
			)
		},
	}

	return command
}
