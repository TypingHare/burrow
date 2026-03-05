package blueprint

import (
	"encoding/json"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func ShowCommand(chamber *kernel.Chamber) *cobra.Command {
	var showJson bool

	command := &cobra.Command{
		Use:   "show",
		Short: "Display the blueprint",
		RunE: func(cmd *cobra.Command, args []string) error {
			if showJson {
				data, err := json.MarshalIndent(chamber.Blueprint(), "", "  ")
				if err != nil {
					return err
				}

				cmd.Println(string(data))
				return nil
			}

			cmd.Println("Not implemented yet")

			return nil
		},
	}

	command.Flags().BoolVarP(
		&showJson, "json", "j", false,
		"Output the blueprint in JSON format",
	)

	return command
}
