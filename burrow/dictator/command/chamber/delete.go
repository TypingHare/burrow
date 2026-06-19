package chamber

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func DeleteCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "delete <chamber>",
		Short: "Delete a chamber",
		Long: strings.TrimSpace(`
This command shuts down a chamber and removes it from memory.

Before the chamber is removed, Burrow terminates and disassembles its
decors and persists the chamber blueprint.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := args[0]
			err := decor.Chamber().Burrow.Architect.Delete(chamberName)
			if err != nil {
				return fmt.Errorf("failed to delete chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
