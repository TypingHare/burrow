package carton

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/api"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(decor share.IDecor) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show cartons used by the chamber",
		Long: strings.TrimSpace(`
This command lists names of carton used by the chamber.

By default, it only displays the cartons used by the current chamber. If you
pass "--all", it instead displays all cartons registered in Burrow's warehouse.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			chamber := decor.Chamber()
			cartonNames, err := api.GetCartonNames(chamber, all)
			if err != nil {
				return fmt.Errorf("failed to get carton names: %w", err)
			}

			printCartonNamesAndVersions(
				chamber.Burrow.Warehouse,
				cartonNames,
				cmd,
			)

			return nil
		},
	}

	command.Flags().BoolVarP(&all, "all", "a", false,
		"Display all cartons registered in the warehouse",
	)

	return command
}

// printCartonNamesAndVersions prints the names and versions of the specified
// cartons.
func printCartonNamesAndVersions(
	warehouse *kernel.Warehouse,
	cartonNames []string,
	formatter interface {
		Printf(format string, args ...any)
	},
) error {
	for _, cartonName := range cartonNames {
		carton, exists := warehouse.CartonsByNames[cartonName]
		if !exists {
			return fmt.Errorf(
				"Carton %q does not exist",
				cartonName,
			)
		}

		version := carton.Metadata.Get(kernel.MetadataVersion)
		formatter.Printf("%s  %s\n", cartonName, version)
	}

	return nil
}
