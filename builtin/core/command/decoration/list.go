package decoration

import (
	"github.com/TypingHare/burrow/v2026/internal/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "list",
		Short: "Show decorations in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			for decorationId := range chamber.Renovator().Decorations() {
				cmd.Printf("%s\n", decorationId)
			}

			return nil
		},
	}

	return command
}
