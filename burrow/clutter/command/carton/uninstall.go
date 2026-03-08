package carton

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func UninstallCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "uninstall <carton>",
		Short: "Uninstall a carton",
		Long: strings.TrimSpace(`
This command removes a carton from the current clutter decoration spec.

After a carton is uninstalled, Burrow no longer includes it when building
the full Burrow executable from this chamber's clutter configuration.

This command also removes any local path that was recorded for the same
carton.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonName := args[0]
			return service.UninstallCarton(d, cartonName)
		},
	}

	return command
}
