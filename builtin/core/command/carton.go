package command

import (
	"github.com/TypingHare/burrow/builtin/core/command/carton"
	"github.com/TypingHare/burrow/internal/kernel"
	"github.com/spf13/cobra"
)

func CartonCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use: "carton",
	}

	command.AddCommand(carton.ListCommand(chamber))

	return command
}
