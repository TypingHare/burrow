package command

import (
	"maps"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func EnvCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "env",
		Short: "Display environment variables",
		Long: strings.TrimSpace(`
Display Burrow environment variables.
        `),
		Args: cobra.NoArgs,
		RunE: func(cmd *cobra.Command, args []string) error {
			env := decor.Chamber().Burrow.Env
			for _, key := range slices.Sorted(maps.Keys(env)) {
				cmd.Println(key + "=" + env[key])
			}

			return nil
		},
	}

	return command
}
