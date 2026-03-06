package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command/chamber"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

// ChamberCommand builds the `chamber` command group for chamber lifecycle
// management.
func ChamberCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "chamber",
		Short: "Manage chambers in Burrow",
	}

	command.AddCommand(chamber.CreateCommand(d))
	command.AddCommand(chamber.DestroyCommand(d))
	command.AddCommand(chamber.BuryCommand(d))
	command.AddCommand(chamber.DigCommand(d))
	command.AddCommand(chamber.InfoCommand(d))
	command.AddCommand(chamber.ListCommand(d))

	return command
}
