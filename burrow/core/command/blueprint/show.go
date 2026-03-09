package blueprint

import (
	"fmt"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

// ShowCommand builds the `blueprint show` command.
func ShowCommand(d share.CoreDecorationLike) *cobra.Command {
	var showJson bool

	command := &cobra.Command{
		Use:   "show",
		Short: "Show the blueprint",
		Long: strings.TrimSpace(`
This command prints the current chamber blueprint.

The blueprint is the configuration that Burrow uses to describe the
decorations installed in the chamber and the raw spec stored for each
one. Right now the command prints JSON output, including the default
output mode.
		`),
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
