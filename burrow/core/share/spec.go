package share

import "github.com/TypingHare/burrow/v2026/kernel"

type CoreSpec struct {
	DirectDependencies []string
}

func ParseCoreSpec(rawSpec kernel.RawSpec) (CoreSpec, error) {
	return CoreSpec{
		DirectDependencies: []string{},
	}, nil
}
