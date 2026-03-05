package share

import "github.com/TypingHare/burrow/v2026/kernel"

type DictatorDecorationLike interface {
	kernel.DecorationInstance
	Spec() *DictatorSpec
}

type DictatorSpec struct{}

func ParseDictatorSpec(rawSpec kernel.RawSpec) (DictatorSpec, error) {
	return DictatorSpec{}, nil
}
