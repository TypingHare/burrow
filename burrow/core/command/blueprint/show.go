package blueprint

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/core/api"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/spf13/cobra"
)

func ShowCommand(decor share.IDecor) *cobra.Command {
	var outputJson bool

	command := &cobra.Command{
		Use:   "show",
		Short: "Show the blueprint",
		Long: strings.TrimSpace(`
This command displays the current chamber blueprint.

By default, this command prints the blueprint in TOML format. To output the
blueprint in JSON format, use the --json flag.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			blueprint := decor.Chamber().Blueprint

			if outputJson {
				jsonString, err := api.BlueprintToJSON(blueprint)
				if err != nil {
					return err
				}

				cmd.Println(jsonString)
				return nil
			}

			tomlString, err := api.BlueprintToTOML(blueprint)
			if err != nil {
				return err
			}
			cmd.Print(tomlString)

			return nil
		},
	}

	command.Flags().BoolVarP(
		&outputJson, "json", "j", false,
		"Output the blueprint in JSON format",
	)

	return command
}
