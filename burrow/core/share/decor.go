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

// GetDecorIDByDecorName resolves a decor name to a decor ID by searching every
// decor definition registered in the warehouse. The second return value lists
// all decor IDs whose name component matches decorName, sorted for
// deterministic output. The first return value is the single unambiguous match,
// or an empty string when there is no match or more than one (i.e. the name is
// ambiguous), in which case the caller should inspect the candidate list.
func GetDecorIDByDecorName(
	coreDecor IDecor,
	decorName string,
) (string, []string) {
	decorDefsByIDs := coreDecor.Chamber().Burrow.Warehouse.DecorDefsByIDs

	matchingDecorIDs := []string{}
	for decorID := range decorDefsByIDs {
		name, _, err := kernel.SplitDecorID(decorID)
		if err != nil {
			continue
		}

		if name == decorName {
			matchingDecorIDs = append(matchingDecorIDs, decorID)
		}
	}
	slices.Sort(matchingDecorIDs)

	uniqueMatch := ""
	if len(matchingDecorIDs) == 1 {
		uniqueMatch = matchingDecorIDs[0]
	}

	return uniqueMatch, matchingDecorIDs
}
