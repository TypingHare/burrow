package kernel

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
)

// Architect manages chambers for a Burrow.
type Architect struct {
	// Burrow is the Burrow managed by the Architect.
	Burrow *Burrow

	// ChambersByNames stores chambers by name.
	ChambersByNames map[string]*Chamber
}

// NewArchitect returns an Architect for the provided Burrow.
func NewArchitect(burrow *Burrow) *Architect {
	return &Architect{
		Burrow:          burrow,
		ChambersByNames: make(map[string]*Chamber),
	}
}

// GetBlueprintPath returns the blueprint file path for chamberName.
func (a *Architect) GetBlueprintPath(chamberName string) string {
	return filepath.Join(
		a.Burrow.GetConfigDir(),
		chamberName,
		a.Burrow.Env.Get(EnvBlueprintFileName),
	)
}

// GetBlueprint loads the blueprint file for chamberName from disk.
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

// SaveBlueprint writes blueprint for chamberName to disk.
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
//
// For the root chamber, Dig falls back to GetDefaultRootChamberBlueprint when
// no blueprint file exists yet.
func (a *Architect) Dig(chamberName string) (*Chamber, error) {
	isRootChamber := chamberName == a.Burrow.Env.Get(EnvRootChamber)
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

	chamber := NewChamber(a.Burrow, chamberName, blueprint)
	if err = chamber.init(); err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to initialize chamber",
			err,
		)
	}

	a.ChambersByNames[chamberName] = chamber

	return chamber, nil
}

func GetDefaultRootChamberBlueprint() Blueprint {
	return Blueprint{}
}
