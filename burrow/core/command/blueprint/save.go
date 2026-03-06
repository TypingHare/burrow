package blueprint

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// SaveCommand builds the `blueprint save` command, which persists the current
// in-memory blueprint without refreshing it from installed decorations.
func SaveCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "save",
		Short: "Save the blueprint",
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
