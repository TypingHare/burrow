package command

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/server/share"
	"github.com/spf13/cobra"
)

// StartCommand builds the `server start` command.
func StartCommand(d share.ServerDecorationLike) *cobra.Command {
	return &cobra.Command{
		Use:   "start",
		Short: "Start the HTTP server",
		Long: strings.TrimSpace(`
This command starts the chamber's HTTP server and keeps it running in the
foreground.

The command does not return until the server is stopped. Press Ctrl+C to
shut it down gracefully.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := d.Start(); err != nil {
				return fmt.Errorf("failed to start server: %w", err)
			}

			return nil
		},
	}
}
