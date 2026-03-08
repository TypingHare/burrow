package share

import "github.com/TypingHare/burrow/v2026/kernel"

// LarderDecorationLike describes the larder-decoration surface required by
// cabinet helpers.
type LarderDecorationLike interface {
	kernel.DecorationInstance
	Spec() *LarderSpec
	CabinetMap() map[string]any
}

// LarderSpec contains persisted configuration for the larder decoration.
type LarderSpec struct{}

// ParseLarderSpec parses raw blueprint data into a LarderSpec.
func ParseLarderSpec(rawSpec kernel.RawSpec) (*LarderSpec, error) {
	return &LarderSpec{}, nil
}
