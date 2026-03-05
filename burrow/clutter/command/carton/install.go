package carton

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func InstallCommand(
	chamber *kernel.Chamber,
	clutterDecoration share.ClutterDecorationLike,
) *cobra.Command {
	var path string

	command := &cobra.Command{
		Use:   "install <carton>",
		Short: "Install a carton",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			err := share.InstallCarton(
				chamber,
				clutterDecoration.Spec(),
				args[0],
				path,
			)
			if err != nil {
				return fmt.Errorf("failed to install carton: %w", err)
			}

			return nil
		},
	}

	command.Flags().StringVar(
		&path, "path", "", "Local path for this carton")

	return command
}
