package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command/chamber"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func ChamberCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "chamber",
		Short: "Manage chambers in Burrow",
	}

	command.AddCommand(chamber.BuryCommand(d))
	command.AddCommand(chamber.DigCommand(d))
	command.AddCommand(chamber.InfoCommand(d))
	command.AddCommand(chamber.ListCommand(d))

	return command
}
