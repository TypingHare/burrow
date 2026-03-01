package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func RedigCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "redig",
		Short: "Redig the chamber",
		Long: strings.TrimSpace(`
Redig the chamber by first burying it and then digging it again.
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			return share.Redig(chamber)
		},
	}

	return command
}
