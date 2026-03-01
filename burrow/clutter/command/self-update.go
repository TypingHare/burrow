package command

import (
	"fmt"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

func SelfUpdateCommand(
	chamber *kernel.Chamber,
	clutterDecoration share.ClutterDecorationLike,
) *cobra.Command {
	command := &cobra.Command{
		Use:   "self-update",
		Short: "Update Burrow to the latest version",
		RunE: func(cmd *cobra.Command, args []string) error {
			cartonNames := clutterDecoration.Spec().CartonNames
			localCartons := clutterDecoration.Spec().LocalCartons

			burrowSourceDir := filepath.Join(
				chamber.Burrow().GetSourceDir(),
				kernel.CartonName,
			)
			outputExecutablePath := filepath.Join(
				chamber.Burrow().GetBinDir(),
				chamber.Burrow().Env.Get(kernel.EnvExecutablePath),
			)
			originalCommit, err := share.BurrowSelfUpdate(
				burrowSourceDir,
				outputExecutablePath,
				cartonNames,
				localCartons,
			)
			if err != nil {
				return chamber.Error(
					"failed to update Burrow source and rebuild",
					err,
				)
			}

			rollback := func() error {
				_, stderr, exitCode, err := coreShare.RunExternalCommand(
					burrowSourceDir,
					[]string{"git", "checkout", originalCommit},
				)
				if err != nil || exitCode != 0 {
					return chamber.Error(
						fmt.Sprintf(
							"failed to roll back to original commit %q",
							originalCommit,
						),
						fmt.Errorf("%s", stderr),
					)
				}

				return nil
			}

			if err := share.BuildBurrow(
				burrowSourceDir,
				cartonNames,
				localCartons,
				outputExecutablePath,
			); err != nil {
				if rollbackErr := rollback(); rollbackErr != nil {
					return chamber.Error(
						"updated source and failed to rebuild Burrow; revert "+
							"also failed",
						fmt.Errorf("%w: %w", err, rollbackErr),
					)
				}

				return chamber.Error(
					fmt.Sprintf(
						"failed to rebuild Burrow; reverted to %q",
						originalCommit,
					),
					err,
				)
			}

			return nil
		},
	}

	return command
}
