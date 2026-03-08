package share

import "github.com/TypingHare/burrow/v2026/kernel"

type ServerDecorationLike interface {
	kernel.DecorationInstance
	Spec() *ServerSpec
}

type ServerSpec struct {
	Port int
}

func ParseServerSpec(rawSpec kernel.RawSpec) (*ServerSpec, error) {
	return &ServerSpec{}, nil
}
