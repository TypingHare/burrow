package command

import (
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func CartonCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		RunE: func(cmd *cobra.Command, args []string) error {
			return nil
		},
	}

	return command
}
