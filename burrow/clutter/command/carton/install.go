package carton

import (
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
		Use:   "install <carton-name>",
		Short: "Install a carton",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonName := args[0]
			spec := clutterDecoration.Spec()
			spec.CartonNames = append(spec.CartonNames, cartonName)

			if path != "" {
				spec.LocalCartons = append(spec.LocalCartons, share.LocalCarton{
					Name: cartonName,
					Path: path,
				})
			}

			// TODO: Try to build the Burrow executable
			// If successful, then done
			// Otherwise, revert the spec to its original state and return error

			return nil
		},
	}

	command.Flags().StringVar(
		&path, "path", "", "Use a local path for this carton")

	return command
}
