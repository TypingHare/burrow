package command

import (
	"strings"

	decorCommands "github.com/TypingHare/burrow/v2026/burrow/core/command/decor"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func DecorCommand(decor share.IDecor) *cobra.Command {
	command := &cobra.Command{
		Use:   "decor",
		Short: "Manage decors in the chamber",
		Long: strings.TrimSpace(`
This command group manages decors in the chamber.

Decors are the chamber features that Burrow installs from cartons. Use these
commands to add decors, list what is installed, or remove direct decor 
dependencies from the chamber.
		`),
		Args: cobra.NoArgs,
	}

	command.AddCommand(decorCommands.AddCommand(decor))
	command.AddCommand(decorCommands.ListCommand(decor))
	command.AddCommand(decorCommands.RemoveCommand(decor))

	return command
}
