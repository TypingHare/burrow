package chamber

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func DestroyCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "destroy <chamber>",
		Short: "Destroy a chamber blueprint",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.DestroyChamber(d.Chamber(), args[0]); err != nil {
				return fmt.Errorf("failed to destroy chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
