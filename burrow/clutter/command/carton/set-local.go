package carton

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func SetLocalCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "set-local <carton> <path>",
		Short: "Set a local path for an installed carton",
		Args:  cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartoName := args[0]
			localPath := args[1]
			return service.SetLocal(d, cartoName, localPath)
		},
	}

	return command
}
