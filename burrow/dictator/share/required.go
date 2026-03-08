package share

import "github.com/TypingHare/burrow/v2026/kernel"

// DictatorDecorationLike describes the surface that dictator commands and
// helpers need from a dictator decoration instance.
type DictatorDecorationLike interface {
	kernel.DecorationInstance
	Spec() *DictatorSpec
}

// DictatorSpec contains persisted configuration for the dictator decoration.
type DictatorSpec struct{}

// ParseDictatorSpec parses raw spec data into a DictatorSpec.
func ParseDictatorSpec(rawSpec kernel.RawSpec) (*DictatorSpec, error) {
	return &DictatorSpec{}, nil
}
