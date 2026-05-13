package decor

import (
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func ListCommand(decor share.IDecor) *cobra.Command {
	var all bool
	var direct bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Display all decors in the chamber",
		Long: strings.TrimSpace(`
This command displays IDs of installed decors in the chamber.

By default, it displays the decors installed in the current chamber. Pass
"--root" to display only the direct dependencies, which are the decors that are
directly installed by users.

Pass "--all" to display all decor definitions registered in the warehouse.

Decor IDs are displayed in alphabetical order.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			var decorIDs []string
			if all {
				decorIDs = share.GetAllDecorIDs(decor.Chamber())
			} else if direct {
				decorIDs = share.GetDirectDecorIDs(decor)
			} else {
				decorIDs = share.GetChamberDecorIDs(decor.Chamber())
			}

			slices.Sort(decorIDs)
			for _, decorID := range decorIDs {
				cmd.Println(decorID)
			}

			return nil
		},
	}

	command.Flags().BoolVarP(&all, "all", "a", false,
		"Display all decors registered in the warehouse",
	)

	command.Flags().BoolVarP(&direct, "direct", "d", false,
		"Display direct decors installed in the chamber",
	)

	return command
}
