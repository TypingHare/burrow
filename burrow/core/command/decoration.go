package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/command/decoration"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func DecorationCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "decoration",
		Short: "Manage decorations in the chamber",
		Long: strings.TrimSpace(`
This command group manages decorations in the current chamber.

Decorations are the chamber features that Burrow installs from cartons.
Use these commands to add decorations, list what is installed, or remove
direct decoration dependencies from the chamber.
		`),
	}

	command.AddCommand(decoration.AddCommand(d))
	command.AddCommand(decoration.ListCommand(d))
	command.AddCommand(decoration.RemoveCommand(d))

	return command
}
