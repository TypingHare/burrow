package api

import (
	"bytes"
	"encoding/json"
	"fmt"

	"github.com/BurntSushi/toml"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// SaveChamberBlueprint saves the Chamber's Blueprint to its TOML file.
func SaveChamberBlueprint(chamber *kernel.Chamber) error {
	err := chamber.Blueprint.SaveToTomlFile(
		chamber.Burrow.Architect.GetBlueprintPath(chamber.Name),
	)
	if err != nil {
		return fmt.Errorf(
			"failed to save blueprint for chamber %q: %w",
			chamber.Name,
			err,
		)
	}

	return nil
}

// BlueprintToTOML returns a TOML representation of blueprint.
func BlueprintToTOML(blueprint kernel.Blueprint) (string, error) {
	var buffer bytes.Buffer
	if err := toml.NewEncoder(&buffer).Encode(blueprint); err != nil {
		return "", fmt.Errorf("failed to marshal blueprint to TOML: %w", err)
	}

	return buffer.String(), nil
}

// BlueprintToJSON returns an indented JSON representation of blueprint.
func BlueprintToJSON(blueprint kernel.Blueprint) (string, error) {
	data, err := json.MarshalIndent(blueprint, "", "  ")
	if err != nil {
		return "", fmt.Errorf("failed to marshal blueprint to JSON: %w", err)
	}

	return string(data), nil
}
