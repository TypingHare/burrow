package chamber

import (
	"strings"

	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func RedigCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "redig",
		Short: "Redig the current chamber",
		Long: strings.TrimSpace(`
This command rebuilds the current chamber in place.

Burrow first buries the current chamber instance and then digs it again
from the persisted blueprint. Use this when you want to reload the chamber
without restarting the whole Burrow process.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			_, err := coreShare.Redig(d.Chamber())
			return err
		},
	}

	return command
}
