package share

import (
	"maps"
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
	"github.com/spf13/cobra"
)

// IDecor represents the ID of a decor definition or an installed decor.
type IDecor interface {
	kernel.IDecor
	RootCommand() *cobra.Command
	DirectDependencies() []string
	SetDirectDependencies([]string)
}

// GetChamberDecorIDs returns the IDs of all decors installed in the given
// chamber.
func GetChamberDecorIDs(chamber *kernel.Chamber) []string {
	return slices.Collect(maps.Keys(chamber.Renovator.DecorsByIDs))
}

// GetAllDecorIDs returns the IDs of all decor definitions registered
// in the warehouse.
func GetAllDecorIDs(chamber *kernel.Chamber) []string {
	return slices.Collect(maps.Keys(
		chamber.Burrow.Warehouse.DecorDefsByIDs,
	))
}

// GetDirectDecorIDs returns the IDs of all decors directly installed by users.
func GetDirectDecorIDs(coreDecor IDecor) []string {
	return coreDecor.DirectDependencies()
}
