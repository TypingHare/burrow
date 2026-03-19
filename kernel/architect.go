package kernel

import (
	"fmt"
	"maps"
	"os"
	"path/filepath"
	"slices"
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

// GetBlueprint retrieves the Blueprint for the specified chamberName.
func (a *Architect) GetBlueprint(chamberName string) (Blueprint, error) {
	blueprint := Blueprint{}

	blueprintPath := a.GetBlueprintPath(chamberName)
	if _, err := os.Stat(blueprintPath); err != nil {
		if os.IsNotExist(err) {
			return nil, NewChamberError(
				chamberName,
				fmt.Sprintf(
					"blueprint file %q does not exist",
					blueprintPath,
				),
				nil,
			)
		}

		return nil, NewChamberError(
			chamberName,
			fmt.Sprintf(
				"failed to check if blueprint file %q exists",
				blueprintPath,
			),
			nil,
		)
	}

	if err := blueprint.LoadFromJSONFile(blueprintPath); err != nil {
		return nil, NewChamberError(
			chamberName,
			fmt.Sprintf(
				"failed to load blueprint from JSON file at path %q",
				blueprintPath,
			),
			err,
		)
	}

	return blueprint, nil
}

// Dig creates a Chamber with the given chamberName.
func (a *Architect) Dig(chamberName string) (*Chamber, error) {
	blueprint, err := a.GetBlueprint(chamberName)
	if err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to load blueprint",
			err,
		)
	}

	chamber := NewChamber(a.Burrow, chamberName, blueprint)
	dependencyDecorIDs := slices.Collect(maps.Keys(chamber.Blueprint))
	err = chamber.Renovator.ResolveDirectDependencies(dependencyDecorIDs)
	if err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to resolve direct dependencies",
			err,
		)
	}

	a.ChambersByNames[chamberName] = chamber

	return chamber, nil
}

// Bury removes the Chamber with the given chamberName from the ChamberMap.
func (a *Architect) Bury(chamberName string) error {
	if _, exists := a.ChambersByNames[chamberName]; !exists {
		return NewChamberError(
			chamberName,
			"failed to burry chamber because it is not dug",
			nil,
		)
	}

	delete(a.ChambersByNames, chamberName)

	return nil
}

// Get retrieves the Chamber with the given chamberName from the ChamberMap.
func (a *Architect) Get(chamberName string) (*Chamber, error) {
	chamber, exists := a.ChambersByNames[chamberName]
	if !exists {
		return nil, NewChamberError(
			chamberName,
			"failed to get chamber because it is not dug",
			nil,
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
