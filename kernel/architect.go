package kernel

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
)

// Architect manages chambers for a Burrow.
type Architect struct {
	// burrow is the Burrow managed by the Architect.
	burrow *Burrow

	// chamberMap stores chambers by name.
	chamberMap map[string]*Chamber
}

// NewArchitect returns an Architect for the provided Burrow.
func NewArchitect(burrow *Burrow) *Architect {
	return &Architect{
		burrow:     burrow,
		chamberMap: make(map[string]*Chamber),
	}
}

// Burrow returns the Burrow managed by a.
func (a *Architect) Burrow() *Burrow {
	return a.burrow
}

// ChamberMap returns the Architect's chambers keyed by name.
func (a *Architect) ChamberMap() map[string]*Chamber {
	return a.chamberMap
}

// GetBlueprintPath returns the blueprint file path for chamberName.
func (a *Architect) GetBlueprintPath(chamberName string) string {
	return filepath.Join(
		a.burrow.GetConfigDir(),
		chamberName,
		a.burrow.Env.Get(EnvBlueprintFileName),
	)
}

// GetBlueprint loads the blueprint for chamberName from disk.
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

// SaveBlueprint saves blueprint for chamberName to disk.
func (a *Architect) SaveBlueprint(
	chamberName string,
	blueprint Blueprint,
) error {
	blueprintFilePath := a.GetBlueprintPath(chamberName)
	if err := blueprint.SaveToJSONFile(blueprintFilePath); err != nil {
		return fmt.Errorf(
			"failed to save blueprint to JSON file: %w",
			err,
		)
	}
	return nil
}

// Dig loads the blueprint for chamberName, initializes the chamber, and stores
// it in the Architect.
func (a *Architect) Dig(chamberName string) (*Chamber, error) {
	isRootChamber := chamberName == a.burrow.Env.Get(EnvRootChamber)
	blueprint, err := a.GetBlueprint(chamberName)
	if err != nil {
		if isRootChamber && errors.Is(err, os.ErrNotExist) {
			blueprint = GetDefaultRootChamberBlueprint()
		} else {
			return nil, NewChamberError(
				chamberName,
				"failed to load blueprint",
				err,
			)
		}
	}

	chamber := NewChamber(a.burrow, chamberName, blueprint)
	if err = chamber.init(); err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to initialize chamber",
			err,
		)
	}

	a.chamberMap[chamberName] = chamber

	return chamber, nil
}

// Bury removes chamberName from the Architect.
func (a *Architect) Bury(chamberName string) error {
	chamber, exists := a.chamberMap[chamberName]
	if !exists {
		return NewChamberError(chamberName, "chamber does not exist", nil)
	}

	err := a.SaveBlueprint(chamberName, chamber.Blueprint())
	if err != nil {
		return NewChamberError(
			chamberName,
			"failed to save blueprint before burying chamber",
			err,
		)
	}

	if err := chamber.discardDecorations(); err != nil {
		return NewChamberError(
			chamberName,
			"failed to terminate chamber",
			err,
		)
	}

	delete(a.chamberMap, chamberName)

	return nil
}

// Get returns the chamber named chamberName.
func (a *Architect) Get(chamberName string) (*Chamber, error) {
	chamber, exists := a.chamberMap[chamberName]
	if !exists {
		return nil, NewChamberError(chamberName, "chamber does not exist", nil)
	}

	return chamber, nil
}

// GetOrDig returns the chamber named chamberName, creating it if necessary.
func (a *Architect) GetOrDig(chamberName string) (*Chamber, error) {
	chamber, err := a.Get(chamberName)
	if err != nil {
		return a.Dig(chamberName)
	}

	return chamber, nil
}

// If the root chamber's blueprint file does not exist,
// GetDefaultRootChamberBlueprint returns a default blueprint with the core and
// clutter specs.
func GetDefaultRootChamberBlueprint() Blueprint {
	return Blueprint{
		"core@github.com/TypingHare/burrow":    NewRawSpec(),
		"clutter@github.com/TypingHare/burrow": NewRawSpec(),
	}
}
