package burrow

import (
	"strings"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/service"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/spf13/cobra"
)

func SelfUpdateCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "self-update",
		Short: "Update Burrow to the latest version",
		Long: strings.TrimSpace(`
This command updates Burrow's own source code and then rebuilds the Burrow
executable.

It works on the local Burrow source checkout stored in Burrow's source
directory. First, it updates that checkout to a newer upstream revision.
After that, it rebuilds the executable using the cartons currently listed
in the clutter decoration spec.

In other words, this command does two things:

    1. Update Burrow's source repository.
    2. Rebuild the Burrow executable from that updated source.

If the rebuild fails after the source code has been updated, the current
implementation attempts to roll the source checkout back to the original
commit. This is a safety measure so you do not get stuck halfway through
an update.

This command updates Burrow itself. It does not install or remove cartons
from the clutter spec. If you want to change which cartons are included in
the rebuilt executable, update the clutter spec first and then run this
command.
		`),
		RunE: func(cmd *cobra.Command, args []string) error {
			return service.BurrowSelfUpdate(d)
		},
	}

	return command
}
