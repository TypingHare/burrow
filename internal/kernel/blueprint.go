package kernel

import (
	"encoding/json"
	"fmt"
	"os"
)

const BlueprintDependencies = "dependencies"

// Blueprint is the configuration for a Chamber, defining its structure and
// functionality.
type Blueprint map[string]any

// NewBlueprint creates a new empty Blueprint.
func NewBlueprint() Blueprint {
	return make(Blueprint)
}

// LoadFromJSONFile loads a Blueprint from a JSON file at the given path.
func (b Blueprint) LoadFromJSONFile(path string) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return fmt.Errorf("failed to read blueprint file: %w", err)
	}

	if err := json.Unmarshal(data, &b); err != nil {
		return fmt.Errorf("failed to unmarshal blueprint JSON: %w", err)
	}

	return nil
}

// GetDependencies retrieves the list of dependencies from the Blueprint. It
// returns an error if the dependencies field is not an array of strings.
func (b Blueprint) GetDependencies() ([]string, error) {
	raw, exists := b[BlueprintDependencies]
	if !exists {
		return []string{}, nil
	}

	if dependencies, ok := raw.([]string); ok {
		return dependencies, nil
	}

	rawDependencies, ok := raw.([]any)
	if !ok {
		return nil, fmt.Errorf("dependencies must be an array of strings")
	}

	dependencies := make([]string, len(rawDependencies))
	for i, dependency := range rawDependencies {
		depName, ok := dependency.(string)
		if !ok {
			return nil, fmt.Errorf("dependencies[%d] must be a string", i)
		}

		dependencies[i] = depName
	}

	return dependencies, nil
}

// GetRawSpec retrieves the RawSpec for a given decoration name from the
// Blueprint. It returns an error if the raw spec is not a RawSpec.
func (b Blueprint) GetRawSpec(decorationName string) (RawSpec, error) {
	val, exists := b[decorationName]
	if !exists {
		return make(RawSpec), nil
	}

	rawSpec, ok := val.(RawSpec)
	if !ok {
		return nil, fmt.Errorf(
			"raw spec for decoration '%s' is not a RawSpec",
			decorationName,
		)
	}

	return rawSpec, nil
}
