package command

import (
	serverCommand "github.com/TypingHare/burrow/v2026/burrow/server/command/server"
	"github.com/TypingHare/burrow/v2026/burrow/server/share"
	"github.com/spf13/cobra"
)

func ServerCommand(d share.ServerDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "server",
		Short: "Manage server",
	}

	command.AddCommand(serverCommand.StartCommand(d))

	return command
}
