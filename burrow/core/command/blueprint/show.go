package blueprint

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// ShowCommand builds the `blueprint show` command.
func ShowCommand(d share.CoreDecorationLike) *cobra.Command {
	var showJson bool

	command := &cobra.Command{
		Use:   "show",
		Short: "Show the blueprint",
		RunE: func(cmd *cobra.Command, args []string) error {
			if showJson {
				data, err := d.Chamber().Blueprint().ToJSON()
				if err != nil {
					return fmt.Errorf(
						"failed to convert blueprint to JSON: %w",
						err,
					)
				}

				cmd.Println(string(data))
				return nil
			}

			// TODO: Implement a human-readable format for the blueprint.
			data, err := d.Chamber().Blueprint().ToJSON()
			if err != nil {
				return fmt.Errorf(
					"failed to convert blueprint to JSON: %w",
					err,
				)
			}

			cmd.Println(string(data))
			return nil
		},
	}

	command.Flags().BoolVarP(
		&showJson, "json", "j", false,
		"Output the blueprint in JSON format",
	)

	return command
}
