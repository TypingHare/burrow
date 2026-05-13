package share

import (
	"maps"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// DeepCopyBlueprint returns a deep copy of the given Blueprint.
func DeepCopyBlueprint(blueprint kernel.Blueprint) kernel.Blueprint {
	if blueprint == nil {
		return nil
	}

	blueprintCopy := make(kernel.Blueprint, len(blueprint))
	for decorID, vars := range blueprint {
		blueprintCopy[decorID] = maps.Clone(vars)
	}

	return blueprintCopy
}
