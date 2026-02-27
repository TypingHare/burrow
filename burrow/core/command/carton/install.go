package carton

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func InstallCommand(chamber *kernel.Chamber) *cobra.Command {
	var all bool

	command := &cobra.Command{
		Use:   "install <carton-url>...",
		Short: "Install a carton to Burrow",
		Long: strings.TrimSpace(`
This command installs a carton to Burrow.
        `),
		RunE: func(cmd *cobra.Command, args []string) error {
			burrow := chamber.Burrow()
			_, err := burrow.GetCartonURLs()
			if err != nil {
				return err
			}

			err = burrow.AddCartonsToCartonsFile(args)
			if err != nil {
				return err
			}

			if err = burrow.GenerateMagicGoModFile(); err != nil {
				return err
			}

			if err = burrow.GenerateMagicGoFile(true); err != nil {
				return err
			}

			if err = burrow.Build(true); err != nil {
				return err
			}

			return nil
		},
	}

	command.Flags().BoolVarP(&all, "all", "a", false,
		"Show all cartons in the warehouse",
	)

	return command
}
