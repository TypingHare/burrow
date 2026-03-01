package carton

import (
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func InstallCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "install",
		Short: "Install a carton",
		RunE: func(cmd *cobra.Command, args []string) error {
			return nil
		},
	}

	return command
}
