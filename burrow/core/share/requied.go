package share

import (
	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// CoreDecorationLike describes the core-decoration surface required by helpers
// and command builders in this package.
type CoreDecorationLike interface {
	kernel.DecorationInstance
	Spec() *CoreSpec
	GetRootCommand() *cobra.Command
}

// CoreSpec stores configuration for the core decoration, including the list of
// root decorations explicitly managed by core.
type CoreSpec struct {
	DirectDependencies []string
}

// ParseCoreSpec parses raw blueprint data into a CoreSpec.
func ParseCoreSpec(rawSpec kernel.RawSpec) (*CoreSpec, error) {
	directDependencies, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"directDependencies",
		[]string{"core@" + kernel.CartonName},
	)
	if err != nil {
		return nil, err
	}

	return &CoreSpec{
		DirectDependencies: directDependencies,
	}, nil
}
