package share

import (
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

type CoreDecorationLike interface {
	kernel.DecorationInstance
	Spec() *CoreSpec
	GetRootCommand() *cobra.Command
}

type CoreSpec struct {
	DirectDependencies []string
}

func ParseCoreSpec(rawSpec kernel.RawSpec) (CoreSpec, error) {
	directDependencies, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"directDependencies",
		[]string{"core@" + kernel.CartonName},
	)
	if err != nil {
		return CoreSpec{}, err
	}

	return CoreSpec{
		DirectDependencies: directDependencies,
	}, nil
}
