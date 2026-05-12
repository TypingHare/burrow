package core

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// CreateAndAddDecorDefToCarton creates a DecorDef with the given name and
// createDecorFunc, and adds it to the carton.
func CreateAndAddDecorDefToCarton[T kernel.IDecor](
	carton *kernel.Carton,
	decorName string,
	createDecorFunc func(*kernel.Chamber, kernel.Vars) (T, error),
	postprocessDecorFunc func(*kernel.Chamber, T) error,
) error {
	decorDef := kernel.NewDecorDef(
		decorName,
		func(chamber *kernel.Chamber, spec kernel.Vars) (kernel.IDecor, error) {
			decor, err := createDecorFunc(chamber, spec)
			if err != nil {
				return nil, fmt.Errorf("failed to create decor: %w", err)
			}

			if postprocessDecorFunc != nil {
				err := postprocessDecorFunc(chamber, decor)
				if err != nil {
					return nil, fmt.Errorf(
						"failed to postprocess decor: %w",
						err,
					)
				}
			}

			return decor, nil
		},
	)

	err := carton.AddDecorDef(decorDef)
	if err != nil {
		return fmt.Errorf(
			"failed to register %q decor definition: %w",
			decorName,
			err,
		)
	}

	return nil
}
