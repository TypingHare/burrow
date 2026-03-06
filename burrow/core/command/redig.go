package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// RedigCommand builds the `redig` command that rebuilds the current chamber in
// place by burying and digging it again.
func RedigCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "redig",
		Short: "Redig the chamber",
		Long: strings.TrimSpace(`
Redig the chamber by first burying it and then digging it again.
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			_, err := share.Redig(d.Chamber())
			return err
		},
	}

	return command
}
