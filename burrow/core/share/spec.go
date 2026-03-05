package share

import "github.com/TypingHare/burrow/v2026/kernel"

type CoreSpec struct {
	DirectDependencies []string
}

func ParseCoreSpec(rawSpec kernel.RawSpec) (CoreSpec, error) {
	directDependencies, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"directDependencies",
		[]string{},
	)
	if err != nil {
		return CoreSpec{}, err
	}

	return CoreSpec{
		DirectDependencies: directDependencies,
	}, nil
}
