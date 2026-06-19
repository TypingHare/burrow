package chamber

import (
	"strings"

	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func RecreateCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "recreate",
		Short: "Recreate the current chamber",
		Long: strings.TrimSpace(`
This command recreates the current chamber in place.

Burrow first deletes the current chamber instance and then creates it again
from the persisted blueprint. Use this when you want to reload the chamber
without recreating the whole Burrow process.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			_, err := coreShare.RecreateChamber(decor.Chamber())
			return err
		},
	}

	return command
}
