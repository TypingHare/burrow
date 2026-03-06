package chamber

import (
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

// InfoCommand builds the placeholder `chamber info` command.
func InfoCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "info <chamber>",
		Short: "Show information about a chamber",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			// chamberName := args[0]

			return nil
		},
	}

	return command
}
