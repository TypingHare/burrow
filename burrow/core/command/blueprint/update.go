package blueprint

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// UpdateCommand builds the `blueprint update` command, which refreshes the
// in-memory blueprint from the currently installed decorations.
func UpdateCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "update",
		Short: "Update the blueprint",
		Long: strings.TrimSpace(`
This command refreshes the in-memory blueprint from the decorations that
are currently installed in the chamber.

Use it when decoration state has changed in memory and you want the
blueprint to match that state before you inspect it or save it.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			err := d.Chamber().UpdateBlueprint()
			if err != nil {
				return fmt.Errorf("failed to update blueprint: %w", err)
			}

			return nil
		},
	}

	return command
}
