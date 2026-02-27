package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/command/carton"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func CartonCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use: "carton",
	}

	command.AddCommand(carton.ListCommand(chamber))

	return command
}
