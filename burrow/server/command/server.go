package command

import (
	"strings"

	serverCommand "github.com/TypingHare/burrow/v2026/burrow/server/command/server"
	"github.com/TypingHare/burrow/v2026/burrow/server/share"
	"github.com/spf13/cobra"
)

func ServerCommand(d share.ServerDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "server",
		Short: "Manage server",
		Long: strings.TrimSpace(`
This command group manages the HTTP server provided by the server
decoration.

Use it to start the server in the foreground for the current chamber.
While it is running, the process listens for HTTP command requests and
handles them until you stop it with Ctrl+C.
		`),
	}

	command.AddCommand(serverCommand.StartCommand(d))

	return command
}
