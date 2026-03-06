package command

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/command/carton"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// CartonCommand builds the `carton` command group for inspecting cartons known
// to the current chamber or warehouse.
func CartonCommand(d share.CoreDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "carton",
		Short: "Manage cartons in Burrow",
	}

	command.AddCommand(carton.ListCommand(d))
	return command
}
