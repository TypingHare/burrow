package blueprint

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func PathCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "path",
		Short: "Display the path to the blueprint file",
		Long: strings.TrimSpace(`
This command displays the path to the blueprint file.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamber := decor.Chamber()
			blueprintPath := chamber.Burrow.Architect.GetBlueprintPath(
				chamber.Name,
			)
			cmd.Println(blueprintPath)

			return nil
		},
	}

	return command
}
