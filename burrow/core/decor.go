package core

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type TypedDecor struct {
	*kernel.TypedDecor[share.Spec]
}

func NewDecor(chamber *kernel.Chamber, spec *share.Spec) (kernel.Decor, error) {
	decor := &TypedDecor{
		TypedDecor: kernel.NewDecor(chamber, spec, nil),
	}

	return decor, nil
}

func UseDecor(chamber *kernel.Chamber) (*TypedDecor, error) {
	return nil, nil
}

func RegisterToCarton(carton *kernel.Carton) error {
	err := kernel.AddTypedDecorDef(carton, "core", share.NewSpec, NewDecor)
	if err != nil {
		return fmt.Errorf(
			"failed to register %q decor definition: %w",
			"core",
			err,
		)
	}

	return nil
}
