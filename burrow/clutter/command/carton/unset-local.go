package carton

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func UnsetLocalCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "unset-local <carton>",
		Short: "Unset the local path for an installed carton",
		Long: strings.TrimSpace(`
This command removes the local source path recorded for a carton.

After that, Burrow will stop using the local checkout for that carton.
Future builds will resolve the carton from its normal source location
instead.

This command does not uninstall the carton. It only clears the local path
override.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonName := args[0]
			return service.UnsetLocal(d, cartonName)
		},
	}

	return command
}
