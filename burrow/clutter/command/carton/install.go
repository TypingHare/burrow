package carton

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func InstallCommand(d share.ClutterDecorationLike) *cobra.Command {
	var path string

	command := &cobra.Command{
		Use:   "install <carton>",
		Short: "Install a carton",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonName := args[0]
			return service.InstallCarton(d, cartonName, path)
		},
	}

	command.Flags().StringVar(
		&path, "path", "", "Local path for this carton")

	return command
}
