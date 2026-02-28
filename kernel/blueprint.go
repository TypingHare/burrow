package kernel

import (
	"encoding/json"
	"fmt"
	"maps"
	"os"
	"path/filepath"
	"slices"
)

// Blueprint holds a Chamber's configuration.
type Blueprint map[string]RawSpec

// NewBlueprint returns an empty Blueprint.
func NewBlueprint() Blueprint {
	return make(Blueprint)
}

// LoadFromJSONFile loads Blueprint data from the JSON file at path.
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

// SaveToJSONFile saves Blueprint data to a JSON file at path.
func (b Blueprint) SaveToJSONFile(path string) error {
	data, err := json.MarshalIndent(b, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal blueprint to JSON: %w", err)
	}

	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return fmt.Errorf("failed to create blueprint directory: %w", err)
	}

	if err := os.WriteFile(path, data, 0o644); err != nil {
		return fmt.Errorf("failed to write blueprint to file: %w", err)
	}

	return nil
}

// GetDecorationIDs returns all decoration IDs defined in the Blueprint.
func (b Blueprint) GetDecorationIDs() []string {
	return slices.Collect(maps.Keys(b))
}

// GetRawSpec returns the RawSpec for decorationID.
//
// If decorationID is not present, GetRawSpec returns an empty RawSpec and no
// error.
func (b Blueprint) GetRawSpec(decorationID string) (RawSpec, error) {
	rawSpec, exists := b[decorationID]
	if !exists {
		return make(RawSpec), nil
	}

	return rawSpec, nil
}
