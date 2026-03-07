package share

import "github.com/TypingHare/burrow/v2026/kernel"

type NostalgiaDecorationLike interface {
	kernel.DecorationInstance
	Spec() *NostalgiaSpec
}

type NostalgiaSpec struct{}

func ParseNostalgiaSpec(rawSpec kernel.RawSpec) (NostalgiaSpec, error) {
	return NostalgiaSpec{}, nil
}
