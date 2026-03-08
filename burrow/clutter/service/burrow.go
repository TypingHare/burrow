package service

import (
	"fmt"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	coreShare "github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// BuildBurrow ensures the Burrow source is available and builds either the
// full or minimal executable for the current clutter decoration.
func BuildBurrow(d share.ClutterDecorationLike, minimal bool) error {
	burrow := d.Chamber().Burrow()
	err := share.EnsureSourceDir(burrow, kernel.CartonName)
	if err != nil {
		return fmt.Errorf(
			"Failed to ensure Burrow source directory: %w",
			err,
		)
	}

	if minimal {
		return share.BuildMinimalBurrow(burrow)
	} else {
		return share.BuildBurrow(
			burrow,
			d.Spec().CartonNames,
			d.Spec().LocalCartons,
			d.Spec().MagicEnv,
		)
	}
}

// BurrowSelfUpdate updates the Burrow source checkout, rebuilds the executable,
// and rolls the source tree back if the rebuild fails.
func BurrowSelfUpdate(d share.ClutterDecorationLike) error {
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
}
