package command

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/server/share"
	"github.com/spf13/cobra"
)

// StartCommand builds the `server start` command.
func StartCommand(d share.ServerDecorationLike) *cobra.Command {
	return &cobra.Command{
		Use:   "start",
		Short: "Start the HTTP server",
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := d.Start(); err != nil {
				return fmt.Errorf("failed to start server: %w", err)
			}

			return nil
		},
	}
}
