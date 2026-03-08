package carton

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func SetLocalCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "set-local <carton> <path>",
		Short: "Set a local path for an installed carton",
		Long: strings.TrimSpace(`
This command tells Burrow to use a local directory for a carton.

Use this when you already have the carton source code on your machine and
want Burrow to build from that local checkout.

The carton must already be installed in the clutter decoration spec.
Setting a local path does not install the carton by itself. It only changes
where Burrow looks for that carton's source code during later builds.
		`),
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			cartoName := args[0]
			localPath := args[1]
			return service.SetLocal(d, cartoName, localPath)
		},
	}

	return command
}
