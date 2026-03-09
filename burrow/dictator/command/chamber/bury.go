package chamber

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

// BuryCommand builds the `chamber bury` command.
func BuryCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "bury <chamber>",
		Short: "Bury a chamber",
		Long: strings.TrimSpace(`
This command shuts down a dug chamber and removes it from memory.

Before the chamber is fully buried, Burrow terminates and disassembles
its decorations and persists the chamber blueprint.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := args[0]
			err := d.Chamber().Burrow().Architect().Bury(chamberName)
			if err != nil {
				return fmt.Errorf("failed to bury chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
