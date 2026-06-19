package chamber

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func CreateCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "create <chamber>",
		Short: "Create a chamber in memory",
		Long: strings.TrimSpace(`
This command creates a chamber in memory from its saved blueprint.

Burrow loads the chamber blueprint from disk, then assembles, launches,
and installs its decors so the chamber is ready to use. To create the
blueprint itself, use "blueprint create".
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := args[0]
			if _, err := decor.Chamber().Burrow.Architect.Create(
				chamberName,
			); err != nil {
				return fmt.Errorf("failed to create chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
