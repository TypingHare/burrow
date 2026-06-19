package burrow

import (
	"fmt"
	"maps"
	"slices"
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/api"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func BuildCommand(decor share.IDecor) *cobra.Command {
	var minimal bool
	var chamber string
	var onlyUsedCartons bool

	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		Long: strings.TrimSpace(`
This command builds a Burrow executable from source code.

By default, it builds the full executable. The full executable includes
Burrow and every carton listed in the current clutter decor spec.
Those cartons are linked into the binary at build time.

If you are not sure which cartons will be included, run:

    carton list --all

The output file is written under Burrow's binary directory. The file name
and relative output path come from the "EXECUTABLE_PATH" environment
variable.

If you pass "--minimal", this command builds a smaller executable that
contains Burrow only. It does not include extra cartons from the clutter
spec. The output path for that executable comes from the
"MINIMAL_EXECUTABLE_PATH" environment variable.

Internally, this command uses the Go toolchain in Burrow's source
directory. Before building, Burrow creates temporary "magic" Go files that
describe which cartons should be linked into the executable. These files
exist only to guide the build process.

If you pass "--chamber", this command builds an executable for the specified
chamber. In this mode, the "kernel.EnvUseChamber" environment variable is set to
the chamber name, and the executable name is derived from the chamber name. For
instance, if the chamber name is "demo", then the user can use the executable
like this:

    demo --help

Here, the "demo" executable is essentially a Burrow, but because it always picks
the "demo" chamber on the route, we don't have to specify the chamber
explicitly.

If you also pass "--only-used-cartons", then only the cartons that are used by
the specified chamber are included in the executable. This is determined by
looking at the chamber's blueprint and collecting the cartons that are listed in
the blueprint. 
        `),
		Args: cobra.NoArgs,
		RunE: func(cmd *cobra.Command, args []string) error {
			chamberName := chamber
			if chamberName == "" {
				return api.BuildBurrow(decor, minimal, kernel.NewVars())
			} else {
				env := kernel.NewVars()
				env.Set(kernel.EnvUseChamber, chamberName)

				if !onlyUsedCartons {
					return api.BuildBurrowForChamber(
						decor,
						chamberName,
						nil,
						env,
					)
				}

				// Collect all cartons used by the specified chamber
				architect := decor.Chamber().Burrow.Architect
				blueprint, err := architect.LoadBlueprint(chamberName)
				if err != nil {
					return fmt.Errorf(
						"failed to load blueprint for chamber %q: %w",
						chamberName,
						err,
					)
				}

				cartonNames := slices.Collect(maps.Keys(blueprint))

				// Build the executable with the collected cartons
				err = api.BuildBurrowForChamber(
					decor,
					chamberName,
					cartonNames,
					env,
				)
			}

			return nil
		},
	}

	command.Flags().BoolVarP(&minimal, "minimal", "m", false,
		"Build a minimal version of Burrow without cartons",
	)
	command.Flags().StringVarP(&chamber, "chamber", "c", "",
		"Specific chamber to build",
	)
	command.Flags().BoolVarP(&onlyUsedCartons, "only-used-cartons", "u", false,
		"Build with only the cartons used by the chamber (implies --chamber)",
	)

	return command
}
