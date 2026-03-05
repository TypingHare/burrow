package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/command/decoration"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func DecorationCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "decoration",
		Short: "Manage decorations in the chamber",
	}

	command.AddCommand(decoration.AddCommand(d))
	command.AddCommand(decoration.ListCommand(d))
	command.AddCommand(decoration.RemoveCommand(d))

	return command
}
