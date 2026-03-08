package share

import "github.com/TypingHare/burrow/v2026/kernel"

// RedirectorDecorationLike describes the redirector-decoration surface used by
// redirector helpers.
type RedirectorDecorationLike interface {
	kernel.DecorationInstance
	Spec() *RedirectorSpec
	GetRedirector() Redirector
}

// RedirectorSpec stores redirector behavior flags persisted in the blueprint.
type RedirectorSpec struct {
	SilentlyRedirect bool
}

// ParseRedirectorSpec parses raw blueprint data into a RedirectorSpec.
func ParseRedirectorSpec(rawSpec kernel.RawSpec) (*RedirectorSpec, error) {
	silentlyRedirect, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"silentlyRedirect",
		false,
	)
	if err != nil {
		return nil, err
	}

	return &RedirectorSpec{
		SilentlyRedirect: silentlyRedirect,
	}, nil
}
