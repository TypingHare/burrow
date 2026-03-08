package burrow

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func BuildCommand(d share.ClutterDecorationLike) *cobra.Command {
	var minimal bool

	command := &cobra.Command{
		Use:   "build",
		Short: "Build Burrow executables",
		Long: strings.TrimSpace(`
This command builds a Burrow executable from source code.

By default, it builds the full executable. The full executable includes
Burrow and every carton listed in the current clutter decoration spec.
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
        `),
		Args: cobra.NoArgs,
		RunE: func(cmd *cobra.Command, args []string) error {
			return service.BuildBurrow(d, minimal)
		},
	}

	command.Flags().BoolVarP(&minimal, "minimal", "m", false,
		"Build a minimal version of Burrow without cartons",
	)

	return command
}
