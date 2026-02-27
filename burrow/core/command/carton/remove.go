package carton

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func RemoveCommand(chamber *kernel.Chamber) *cobra.Command {
	command := &cobra.Command{
		Use:   "remove <carton-url>...",
		Short: "remove a carton from Burrow",
		Long: strings.TrimSpace(`
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			burrow := chamber.Burrow()
			_, err := burrow.GetCartonURLs()
			if err != nil {
				return chamber.Error("get carton URLs", err)
			}

			err = burrow.RemoveCartonsFromCartonsFile(args)
			if err != nil {
				return chamber.Error("add cartons to cartons file", err)
			}

			if err = burrow.GenerateMagicGoModFile(); err != nil {
				return chamber.Error("generate magic go.mod file", err)
			}

			if err = burrow.GenerateMagicGoFile(true); err != nil {
				return chamber.Error("generate magic go file", err)
			}

			if err = burrow.Build(true, ""); err != nil {
				return chamber.Error("build Burrow", err)
			}

			return nil
		},
	}

	return command
}
