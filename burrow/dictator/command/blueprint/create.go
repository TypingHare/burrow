package blueprint

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func CreateCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "create <chamber>",
		Short: "Create a new chamber blueprint",
		Long: strings.TrimSpace(`
This command creates a new chamber blueprint on disk.

Creating a chamber does not load it into memory yet. It only writes the
saved blueprint so the chamber can be loaded and used later.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := args[0]
			if err := share.CreateChamberBlueprint(
				decor.Chamber(),
				chamberName,
			); err != nil {
				return fmt.Errorf("failed to create chamber blueprint: %w", err)
			}

			return nil
		},
	}

	return command
}
