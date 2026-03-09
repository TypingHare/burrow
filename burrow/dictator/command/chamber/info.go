package chamber

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func InfoCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "info <chamber>",
		Short: "Show information about a chamber",
		Long: strings.TrimSpace(`
This command is intended to show details about a chamber.

At the moment it is only a placeholder and does not print chamber
information yet.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			// chamberName := args[0]

			return nil
		},
	}

	return command
}
