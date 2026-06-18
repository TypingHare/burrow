package api

import (
	"fmt"
	"path/filepath"
	"slices"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// BuildBurrow ensures the Burrow source is available and builds either the
// full or minimal executable for the current clutter decoration.
func BuildBurrow(
	decor share.IDecor,
	isMinimal bool,
	magicEnv kernel.Vars,
) error {
	burrow := decor.Chamber().Burrow
	err := share.EnsureSourceDir(burrow, kernel.CartonName)
	if err != nil {
		return fmt.Errorf(
			"Failed to ensure Burrow source directory: %w",
			err,
		)
	}

	if isMinimal {
		return share.BuildMinimalBurrow(burrow)
	} else {
		return share.BuildBurrow(
			burrow,
			decor.CartonDefs(),
			magicEnv,
		)
	}
}

func BuildBurrowForChamber(
	decor share.IDecor,
	chamberName string,
	cartonNames []string,
	magicEnv kernel.Vars,
) error {
	burrow := decor.Chamber().Burrow
	err := share.EnsureSourceDir(burrow, kernel.CartonName)
	if err != nil {
		return fmt.Errorf(
			"Failed to ensure Burrow source directory: %w",
			err,
		)
	}

	// Filter the carton definitions to include only those specified in
	// cartonNames.
	filteredCartonDefs := []*share.CartonDef{}
	if cartonNames == nil {
		filteredCartonDefs = slices.Clone(decor.CartonDefs())
	} else {
		for _, cartonDef := range decor.CartonDefs() {
			if slices.Contains(cartonNames, cartonDef.Name) {
				filteredCartonDefs = append(filteredCartonDefs, cartonDef)
			}
		}
	}

	burrowSourceDir := share.GetBurrowSourceDir(burrow)
	outputExecutablePath := filepath.Join(
		burrow.GetBinDir(),
		chamberName,
	)

	return share.NewBuilder(
		burrowSourceDir,
		filteredCartonDefs,
		magicEnv,
		outputExecutablePath,
	).BuildBurrow()
}

// BurrowSelfUpdate updates the Burrow source checkout, rebuilds the executable,
// and rolls the source tree back if the rebuild fails.
func SelfUpdateBurrow(decor share.IDecor, magicEnv kernel.Vars) error {
	burrow := decor.Chamber().Burrow
	cartonDefs := decor.CartonDefs()

	burrowSourceDir := share.GetCartonSourceDir(
		burrow,
		kernel.CartonName,
	)
	outputExecutablePath := filepath.Join(
		burrow.GetBinDir(),
		burrow.Env.Get(kernel.EnvExecutableName),
	)
	originalCommit, err := share.BurrowSelfUpdate(
		burrowSourceDir,
		outputExecutablePath,
		cartonDefs,
	)
	if err != nil {
		return fmt.Errorf(
			"failed to update Burrow source and rebuild: %w",
			err,
		)
	}

	rollback := func() error {
		if err := share.GitCheckout(
			burrowSourceDir,
			originalCommit,
		); err != nil {
			return fmt.Errorf(
				"failed to roll back to original commit %q: %w",
				originalCommit,
				err,
			)
		}

		return nil
	}

	if err := share.NewBuilder(
		burrowSourceDir,
		cartonDefs,
		magicEnv,
		outputExecutablePath,
	).BuildBurrow(); err != nil {
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
