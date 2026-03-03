package command

import (
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func BlueprintCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "blueprint",
		Short: "Display the blueprint of the current chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			// TODO: how?
			return nil
		},
	}

	return command
}
