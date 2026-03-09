package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/command/burrow"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func BurrowCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "burrow",
		Short: "Manage Burrow itself",
		Long: strings.TrimSpace(`
This command group contains commands that act on Burrow itself instead of
on a single chamber feature.

Use these commands when you want to build a Burrow executable or update
the Burrow source checkout and rebuild the executable from newer source.
		`),
	}

	command.AddCommand(burrow.BuildCommand(d))
	command.AddCommand(burrow.SelfUpdateCommand(d))

	return command
}
