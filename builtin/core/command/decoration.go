package command

import (
	"github.com/TypingHare/burrow/builtin/core/command/decoration"
	"github.com/TypingHare/burrow/internal/kernel"
	"github.com/spf13/cobra"
)

func DecorationCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use: "decoration",
	}

	command.AddCommand(decoration.ListCommand(chamber))

	return command
}
