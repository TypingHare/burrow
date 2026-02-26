package kernel

import (
	"encoding/json"
	"fmt"
	"os"
)

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
