package blueprint

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func DeleteCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "delete <chamber>",
		Short: "Delete a chamber blueprint",
		Long: strings.TrimSpace(`
This command deletes a chamber blueprint from disk.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.DeleteChamberBlueprint(
				decor.Chamber(),
				args[0],
			); err != nil {
				return fmt.Errorf("failed to delete chamber blueprint: %w", err)
			}

			return nil
		},
	}

	return command
}
