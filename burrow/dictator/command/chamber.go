package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command/chamber"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func ChamberCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "decoration",
		Short: "Manage decorations in the chamber",
	}

	command.AddCommand(chamber.BuryCommand(d))

	return command
}
