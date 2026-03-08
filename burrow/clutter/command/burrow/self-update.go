package burrow

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func SelfUpdateCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "self-update",
		Short: "Update Burrow to the latest version",
		RunE: func(cmd *cobra.Command, args []string) error {
			return service.BurrowSelfUpdate(d)
		},
	}

	return command
}
