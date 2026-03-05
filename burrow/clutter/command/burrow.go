package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/command/burrow"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func BurrowCommand(
	chamber *kernel.Chamber,
	clutterDecoration share.ClutterDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "burrow",
		Short: "Manage Burrow itself",
	}

	command.AddCommand(burrow.BuildCommand(chamber, clutterDecoration))
	command.AddCommand(burrow.SelfUpdateCommand(chamber, clutterDecoration))

	return command
}
