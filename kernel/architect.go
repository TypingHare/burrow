package kernel

import (
	"fmt"
	"os"
	"path/filepath"
)

// BlueprintFileName is the name of the blueprint file.
const BlueprintFileName = "blueprint.json"

// Architect is responsible for managing the chambers of the burrow.
type Architect struct {
	// burrow is the Burrow that this Architect manages.
	burrow *Burrow

	// chamberMap maps chamber names to their corresponding Chamber instances.
	chamberMap map[string]*Chamber

	// decorationIDs is a slice of strings that holds the IDs of the Decorations
	// in the chamber.
	decorationIDs []string
}

// NewArchitect creates a new Architect for the given Burrow.
func NewArchitect(burrow *Burrow) *Architect {
	return &Architect{
		burrow:     burrow,
		chamberMap: make(map[string]*Chamber),
	}
}

// Burrow returns the Burrow that this Architect manages.
func (a *Architect) Burrow() *Burrow {
	return a.burrow
}

// ChamberMap returns the map of chamber names to their corresponding Chamber
// instances.
func (a *Architect) ChamberMap() map[string]*Chamber {
	return a.chamberMap
}

// GetBlueprintPath returns the path to the blueprint file for the given
// chamberName.
func (a *Architect) GetBlueprintPath(chamberName string) string {
	return filepath.Join(
		a.burrow.GetConfigDir(),
		chamberName,
		BlueprintFileName,
	)
}

// GetBlueprint retrieves the Blueprint for the specified chamberName.
func (a *Architect) GetBlueprint(chamberName string) (Blueprint, error) {
	blueprint := NewBlueprint()

	blueprintFilePath := a.GetBlueprintPath(chamberName)
	if _, err := os.Stat(blueprintFilePath); err != nil {
		if os.IsNotExist(err) {
			return nil, fmt.Errorf("blueprint file does not exist: %w", err)
		}
		return nil, fmt.Errorf(
			"failed to check if blueprint file exists: %w",
			err,
		)
	}

	if err := blueprint.LoadFromJSONFile(blueprintFilePath); err != nil {
		return nil, fmt.Errorf(
			"failed to load blueprint from JSON file: %w",
			err,
		)
	}

	return blueprint, nil
}

// Dig creates a Chamber with the given chamberName.
func (a *Architect) Dig(chamberName string) (*Chamber, error) {
	blueprint, err := a.GetBlueprint(chamberName)
	if err != nil {
		return nil, NewChamberError(chamberName, "load blueprint", err)
	}

	chamber := NewChamber(a.burrow, chamberName, blueprint)
	if err = chamber.Init(); err != nil {
		return nil, NewChamberError(chamberName, "initialize chamber", err)
	}

	a.chamberMap[chamberName] = chamber

	return chamber, nil
}

// Bury removes the Chamber with the given chamberName from the ChamberMap.
func (a *Architect) Bury(chamberName string) error {
	if _, exists := a.chamberMap[chamberName]; !exists {
		return NewChamberError(
			chamberName,
			"bury chamber",
			fmt.Errorf("chamber does not exist"),
		)
	}

	delete(a.chamberMap, chamberName)

	return nil
}

// Get retrieves the Chamber with the given chamberName from the ChamberMap.
func (a *Architect) Get(chamberName string) (*Chamber, error) {
	chamber, exists := a.chamberMap[chamberName]
	if !exists {
		return nil, NewChamberError(
			chamberName,
			"get chamber",
			fmt.Errorf("chamber does not exist"),
		)
	}

	return chamber, nil
}

// GetOrDig retrieves the Chamber with the given chamberName from the
// ChamberMap. If the Chamber does not exist, it digs a new Chamber with the
// given chamberName.
func (a *Architect) GetOrDig(chamberName string) (*Chamber, error) {
	chamber, err := a.Get(chamberName)
	if err != nil {
		return a.Dig(chamberName)
	}

	return chamber, nil
}
