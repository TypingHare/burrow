package carton

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(d share.CoreDecorationLike) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "list",
		Short: "Show cartons used in the chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			warehouse := d.Chamber().Burrow().Warehouse()

			if all {
				allCartonNames := share.GetAllCartonNames(warehouse)
				return printCartonNamesAndVersions(
					warehouse,
					allCartonNames,
					cmd,
				)
			}

			cartonNames, err := share.GetCartonNames(d.Chamber())
			if err != nil {
				return fmt.Errorf("failed to get carton names: %w", err)
			}
			printCartonNamesAndVersions(warehouse, cartonNames, cmd)

			return nil
		},
	}

	command.Flags().BoolVarP(&all, "all", "a", false,
		"Show all cartons in the warehouse",
	)

	return command
}

func printCartonNamesAndVersions(
	warehouse *kernel.Warehouse,
	cartonNames []string,
	formatter interface {
		Printf(format string, args ...any)
	},
) error {
	for _, cartonName := range cartonNames {
		carton, err := warehouse.GetCarton(cartonName)
		if err != nil {
			return fmt.Errorf(
				"failed to get carton %q: %w",
				cartonName,
				err,
			)
		}

		version := carton.Metadata.Get(kernel.MetadataVersion)
		formatter.Printf("%s  %s\n", cartonName, version)
	}

	return nil
}
