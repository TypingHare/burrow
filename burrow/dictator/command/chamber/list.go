package chamber

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/spf13/cobra"
)

func ListCommand(decor share.IDecor) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "List chambers loaded in memory",
		Long: strings.TrimSpace(`
This command lists chamber names.

By default, it shows only chambers that are currently loaded in memory. If
"--all" is provided, it also shows chambers that exist on disk but are not
loaded into memory.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			burrow := decor.Chamber().Burrow

			if all {
				allChamberNames, err := share.GetAllChamberNames(burrow)
				if err != nil {
					return fmt.Errorf(
						"failed to get all chamber names: %w",
						err,
					)
				}

				for _, chamberName := range allChamberNames {
					cmd.Println(chamberName)
				}

				return nil
			}

			for _, chamberName := range share.GetCreatedChamberNames(burrow) {
				cmd.Println(chamberName)
			}

			return nil
		},
	}

	command.Flags().BoolVarP(
		&all, "all", "a", false,
		"List all chambers, including those not loaded into memory",
	)

	return command
}
