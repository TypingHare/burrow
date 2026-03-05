package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func RedigCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "redig",
		Short: "Redig the chamber",
		Long: strings.TrimSpace(`
Redig the chamber by first burying it and then digging it again.
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			return share.Redig(d.Chamber())
		},
	}

	return command
}
