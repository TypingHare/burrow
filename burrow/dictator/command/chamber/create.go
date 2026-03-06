package chamber

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

// CreateCommand builds the `chamber create` command.
func CreateCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "create <chamber>",
		Short: "Create a new chamber blueprint",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.CreateChamber(d.Chamber(), args[0]); err != nil {
				return fmt.Errorf("failed to create chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
