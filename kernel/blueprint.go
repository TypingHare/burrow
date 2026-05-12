package kernel

import (
	"fmt"
	"maps"
	"os"
	"slices"

	"github.com/BurntSushi/toml"
)

// Blueprint maps decor names to their corresponding specs.
type Blueprint map[string]Vars

// NewBlueprint returns a new Blueprint.
func NewBlueprint() Blueprint {
	return make(Blueprint)
}

// GetDecorIDs returns all decor IDs in the Blueprint.
func (b Blueprint) GetDecorIDs() []string {
	return slices.Collect(maps.Keys(b))
}

// LoadFromTomlFile loads the Blueprint from the TOML file at path.
func (b Blueprint) LoadFromTomlFile(path string) error {
	_, err := toml.DecodeFile(path, &b)
	if err != nil {
		return fmt.Errorf("failed to read blueprint file: %w", err)
	}

	return nil
}

// saveToTomlFile saves the Blueprint to the TOML file at path.
func (b Blueprint) SaveToTomlFile(path string) error {
	file, err := os.Create(path)
	if err != nil {
		return fmt.Errorf("failed to create blueprint file: %w", err)
	}

	if err := toml.NewEncoder(file).Encode(b); err != nil {
		_ = file.Close()
		return fmt.Errorf("failed to write blueprint file: %w", err)
	}

	if err := file.Close(); err != nil {
		return fmt.Errorf("failed to close blueprint file: %w", err)
	}

	return nil
}
