package carton

import (
	"github.com/TypingHare/burrow/internal/kernel"
	"github.com/spf13/cobra"
)

func ListCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "list",
		Short: "Display all cartons in the current chamber",
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonMap := chamber.Burrow().Warehouse().CartonMap()
			for id, carton := range cartonMap {
				version := carton.Metadata.Get(kernel.MetadataVersion)
				cmd.Println(id + " - " + version)
			}

			return nil
		},
	}

	return command
}
