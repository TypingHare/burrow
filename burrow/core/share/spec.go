package share

import "github.com/TypingHare/burrow/v2026/kernel"

type Spec struct{}

func NewSpec(raw kernel.RawSpec) (*Spec, error) {
	return &Spec{}, nil
}
