package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/command/decoration"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func DecorationCommand(
	chamber *kernel.Chamber,
	coreDecoration share.CoreDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use: "decoration",
	}

	command.AddCommand(decoration.AddCommand(chamber))
	command.AddCommand(decoration.ListCommand(chamber))
	command.AddCommand(decoration.RemoveCommand(chamber, coreDecoration))

	return command
}
