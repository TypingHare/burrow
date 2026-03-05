package share

import "github.com/TypingHare/burrow/v2026/kernel"

type LarderDecorationLike interface {
	kernel.DecorationInstance
	Spec() *LarderSpec
}

type LarderSpec struct{}

func ParseLarderSpec(rawSpec kernel.RawSpec) (LarderSpec, error) {
	return LarderSpec{}, nil
}
