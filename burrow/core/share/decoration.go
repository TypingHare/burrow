package share

import (
	"slices"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetRootDecorationIDs returns the IDs of root decorations in the given
// renovator.
func GetRootDecorationIDs(renvator *kernel.Renovator) []string {
	dependentDecorationIDs := make(map[string]struct{})
	for _, decoration := range renvator.OrderedDecorations() {
		for _, dependencyID := range decoration.Dependencies() {
			dependentDecorationIDs[dependencyID] = struct{}{}
		}
	}

	rootDecorationIDs := []string{}
	for _, decorationID := range renvator.OrderedDecorationIDs() {
		if _, exists := dependentDecorationIDs[decorationID]; !exists {
			rootDecorationIDs = append(rootDecorationIDs, decorationID)
		}
	}

	return rootDecorationIDs
}

// GetSortedDecorationIDs returns the IDs of all decorations in the given
// chamber, sorted in alphabetical order.
func GetSortedDecorationIDs(chamber *kernel.Chamber) []string {
	orderedDecorationIDs := slices.Clone(
		chamber.Renovator().OrderedDecorationIDs(),
	)
	slices.Sort(orderedDecorationIDs)

	return orderedDecorationIDs
}
