package blueprint

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/api"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func SaveCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "save",
		Short: "Save the blueprint",
		Long: strings.TrimSpace(`
This command writes the current in-memory blueprint to disk.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			return api.SaveChamberBlueprint(decor.Chamber())
		},
	}

	return command
}
