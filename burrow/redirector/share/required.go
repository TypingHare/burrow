package share

import "github.com/TypingHare/burrow/v2026/kernel"

type RedirectorDecorationLike interface {
	kernel.DecorationInstance
	Spec() *RedirectorSpec
}

type RedirectorSpec struct{}

func ParseRedirectorSpec(rawSpec kernel.RawSpec) (RedirectorSpec, error) {
	return RedirectorSpec{}, nil
}
