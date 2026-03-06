package share

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

func GetRootCommand(chamberName string, version string) *cobra.Command {
	command := &cobra.Command{
		Use:     os.Args[0] + " " + chamberName,
		Version: version,
		RunE: func(cmd *cobra.Command, args []string) error {
			if len(args) > 0 {
				return fmt.Errorf(
					"unknown command %q for %q",
					args[0],
					cmd.CommandPath(),
				)
			}

			return nil
		},
	}

	command.SetVersionTemplate("{{.Version}}\n")
	return command
}
