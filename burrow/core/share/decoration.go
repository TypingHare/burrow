package share

import "github.com/TypingHare/burrow/v2026/kernel"

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
