package share

import (
	"fmt"

	"github.com/spf13/cobra"
)

type RedirectionHandler func(cmd *cobra.Command, args []string) error

func GetSimpleRedirectionHandler(
	handler func(cmd *cobra.Command, firstArg string) error,
) RedirectionHandler {
	return func(cmd *cobra.Command, args []string) error {
		if len(args) == 0 {
			return fmt.Errorf("no argument provided for redirection")
		}

		return handler(cmd, args[0])
	}
}
