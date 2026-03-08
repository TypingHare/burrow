package carton

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func InstallCommand(d share.ClutterDecorationLike) *cobra.Command {
	var path string

	command := &cobra.Command{
		Use:   "install <carton>",
		Short: "Install a carton",
		Long: strings.TrimSpace(`
This command adds a carton to the current clutter decoration spec.

After a carton is installed, Burrow remembers it as one of the cartons
that should be available when you build a Burrow executable.

If you pass "--path", Burrow also records a local directory for that
carton. This is useful when you want to build from a local checkout
instead of downloading the carton from its normal source location.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonName := args[0]
			return service.InstallCarton(d, cartonName, path)
		},
	}

	command.Flags().StringVar(
		&path, "path", "", "Local path for this carton")

	return command
}
