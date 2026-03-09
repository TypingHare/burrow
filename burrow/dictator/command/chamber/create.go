package chamber

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func CreateCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "create <chamber>",
		Short: "Create a new chamber blueprint",
		Long: strings.TrimSpace(`
This command creates a new chamber blueprint on disk.

Creating a chamber does not dig it into memory yet. It only creates the
saved blueprint so the chamber can be dug and used later.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.CreateChamber(d.Chamber(), args[0]); err != nil {
				return fmt.Errorf("failed to create chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
