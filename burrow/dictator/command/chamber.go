package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/command/chamber"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func ChamberCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "chamber",
		Short: "Manage chambers in Burrow",
		Long: strings.TrimSpace(`
This command group manages chambers in the current Burrow workspace.

Use these commands to create a chamber in memory, delete a chamber from
memory, recreate a chamber in place, inspect a chamber, or list the
chambers that Burrow knows about.
		`),
	}

	command.AddCommand(chamber.CreateCommand(decor))
	command.AddCommand(chamber.DeleteCommand(decor))
	command.AddCommand(chamber.InfoCommand(decor))
	command.AddCommand(chamber.ListCommand(decor))
	command.AddCommand(chamber.RecreateCommand(decor))

	return command
}
