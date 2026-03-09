package blueprint

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// SaveCommand builds the `blueprint save` command, which persists the current
// in-memory blueprint without refreshing it from installed decorations.
func SaveCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "save",
		Short: "Save the blueprint",
		Long: strings.TrimSpace(`
This command writes the current in-memory blueprint to disk.

Use it when you want to persist the blueprint exactly as it exists right
now. Unlike "blueprint update", this command does not refresh the
blueprint from the installed decorations before saving it.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamber := d.Chamber()
			err := chamber.Burrow().Architect().SaveBlueprint(
				chamber.Name(),
				chamber.Blueprint(),
			)
			if err != nil {
				return fmt.Errorf("failed to save blueprint: %w", err)
			}

			return nil
		},
	}

	return command
}
