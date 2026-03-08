package carton

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func UnsetLocalCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "unset-local <carton>",
		Short: "Unset the local path for an installed carton",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonName := args[0]
			return service.UnsetLocal(d, cartonName)
		},
	}

	return command
}
