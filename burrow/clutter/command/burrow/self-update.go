package burrow

import (
	"fmt"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// SelfUpdateCommand builds the `burrow self-update` command.
func SelfUpdateCommand(d share.ClutterDecorationLike) *cobra.Command {
	command := &cobra.Command{
		Use:   "self-update",
		Short: "Update Burrow to the latest version",
		RunE: func(cmd *cobra.Command, args []string) error {
			burrow := d.Chamber().Burrow()
			cartonNames := d.Spec().CartonNames
			localCartons := d.Spec().LocalCartons
			magicEnv := d.Spec().MagicEnv

			burrowSourceDir := share.GetCartonSourceDir(
				burrow,
				kernel.CartonName,
			)
			outputExecutablePath := filepath.Join(
				burrow.GetBinDir(),
				burrow.Env.Get(kernel.EnvExecutablePath),
			)
			originalCommit, err := share.BurrowSelfUpdate(
				burrowSourceDir,
				outputExecutablePath,
				cartonNames,
				localCartons,
			)
			if err != nil {
				return fmt.Errorf(
					"failed to update Burrow source and rebuild: %w",
					err,
				)
			}

			rollback := func() error {
				_, stderr, exitCode, err := coreShare.RunExternalCommand(
					burrowSourceDir,
					[]string{"git", "checkout", originalCommit},
				)
				if err != nil || exitCode != 0 {
					return fmt.Errorf(
						"failed to roll back to original commit %q: %w",
						originalCommit,
						fmt.Errorf("%s", stderr),
					)
				}

				return nil
			}

			if err := share.NewBuilder(
				burrowSourceDir,
				cartonNames,
				localCartons,
				magicEnv,
				outputExecutablePath,
			).Build(); err != nil {
				if rollbackErr := rollback(); rollbackErr != nil {
					return fmt.Errorf(
						"updated source and failed to rebuild Burrow; revert "+
							"also failed: %w",
						fmt.Errorf("%s: %w", rollbackErr.Error(), err),
					)
				}

				return fmt.Errorf(
					"failed to rebuild Burrow; reverted to %q: %w",
					originalCommit,
					err,
				)
			}

			return nil
		},
	}

	return command
}
