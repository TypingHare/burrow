package command

import (
	"github.com/TypingHare/burrow/v2026/builtin/core/command/decoration"
	"github.com/TypingHare/burrow/v2026/internal/kernel"
	"github.com/spf13/cobra"
)

func DecorationCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use: "decoration",
	}

	command.AddCommand(decoration.ListCommand(chamber))

	return command
}
