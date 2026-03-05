package blueprint

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func UpdateCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "update",
		Short: "Update the blueprint",
		RunE: func(cmd *cobra.Command, args []string) error {
			err := chamber.UpdateBlueprint()
			if err != nil {
				return fmt.Errorf("failed to update blueprint: %w", err)
			}

			return nil
		},
	}

	return command
}
