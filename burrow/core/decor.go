package core

import (
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type Decor struct {
	*kernel.TypedDecor[share.Spec]
}

func NewDecor(chamber *kernel.Chamber, spec *share.Spec) (kernel.Decor, error) {
	decor := &Decor{
		TypedDecor: kernel.NewDecor(chamber, spec, nil),
	}

	return decor, nil
}

func UseDecor(chamber *kernel.Chamber) (*Decor, error) {
	return nil, nil
}

func RegisterToCarton(carton *kernel.Carton) error {
	return kernel.AddTypedDecorDef(
		carton,
		"demo",
		share.NewSpec,
		NewDecor,
	)
}
