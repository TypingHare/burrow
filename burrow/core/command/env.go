package command

import (
	"maps"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func EnvCommand(d kernel.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "env",
		Short: "Display environment variables",
		Long: strings.TrimSpace(`
Display Burrow environment variables.
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			env := d.Chamber().Burrow.Env
			for _, key := range slices.Sorted(maps.Keys(env)) {
				cmd.Println(key + "=" + env[key])
			}

			return nil
		},
	}

	return command
}
