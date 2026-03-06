package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/command/burrow"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

// BurrowCommand builds the `burrow` command group for managing the Burrow
// executable and its source tree.
func BurrowCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "burrow",
		Short: "Manage Burrow itself",
	}

	command.AddCommand(burrow.BuildCommand(d))
	command.AddCommand(burrow.SelfUpdateCommand(d))

	return command
}
