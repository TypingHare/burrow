package decoration

import (
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

			// TODO: this command should add a flag that resolves all cartons
			// required in this decoration
			return share.UpdateBlueprintAndRedig(
				d.Chamber(),
				func(blueprint kernel.Blueprint) error {
					blueprint[decorationID] = kernel.NewRawSpec()
					return nil
				},
			)
		},
	}

	return command
}
