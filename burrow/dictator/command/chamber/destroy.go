package chamber

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func DestroyCommand(d share.DictatorDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "destroy <chamber>",
		Short: "Destroy a chamber blueprint",
		Long: strings.TrimSpace(`
This command deletes a chamber blueprint from disk.

Use it when you want to remove a chamber definition entirely. This is
different from "chamber bury", which only shuts down a dug chamber in the
current process.
		`),
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := share.DestroyChamber(d.Chamber(), args[0]); err != nil {
				return fmt.Errorf("failed to destroy chamber: %w", err)
			}

			return nil
		},
	}

	return command
}
