package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// EnvCommand builds the `env` command that prints the Burrow environment seen
// by the current chamber.
func EnvCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "env",
		Short: "Show environment variables",
		Long: strings.TrimSpace(`
Show Burrow environment variables.
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			for key, value := range d.Chamber().Burrow().Env {
				cmd.Println(key + "=" + value)
			}

			return nil
		},
	}

	return command
}
