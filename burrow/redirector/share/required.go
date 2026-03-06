package share

import "github.com/TypingHare/burrow/v2026/kernel"

type RedirectorDecorationLike interface {
	kernel.DecorationInstance
	Spec() *RedirectorSpec
	GetRedirector() Redirector
}

type RedirectorSpec struct {
	SilentlyRedirect bool
}

func ParseRedirectorSpec(rawSpec kernel.RawSpec) (RedirectorSpec, error) {
	silentlyRedirect, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"silentlyRedirect",
		false,
	)
	if err != nil {
		return RedirectorSpec{}, err
	}

	return RedirectorSpec{
		SilentlyRedirect: silentlyRedirect,
	}, nil
}
