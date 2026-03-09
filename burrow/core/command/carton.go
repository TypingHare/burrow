package command

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/command/carton"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func CartonCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "carton",
		Short: "Manage cartons in Burrow",
		Long: strings.TrimSpace(`
This command group shows information about cartons known to Burrow.

A carton is a bundle of decoration factories. Use these commands to see
which cartons the current chamber depends on or which cartons are
registered in the warehouse.
		`),
	}

	command.AddCommand(carton.ListCommand(d))
	return command
}
