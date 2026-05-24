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

// NewArchitect returns an Architect for burrow.
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

// LoadBlueprint loads the Blueprint for chamberName.
func (a *Architect) LoadBlueprint(chamberName string) (Blueprint, error) {
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

	blueprint := NewBlueprint()
	if err := blueprint.LoadFromTomlFile(blueprintPath); err != nil {
		return nil, NewChamberError(
			chamberName,
			fmt.Sprintf(
				"failed to load blueprint from file %q",
				blueprintPath,
			),
			err,
		)
	}

	return blueprint, nil
}

// UpdateBlueprint updates the Blueprint of chamber based on the current state
// of its decors.
func UpdateBlueprint(chamber *Chamber) error {
	for decorID, decor := range chamber.Renovator.DecorsByIDs {
		if err := decor.UpdateSpec(); err != nil {
			return fmt.Errorf(
				"failed to update spec for decor %q: %w",
				decorID,
				err,
			)
		}
	}

	return nil
}

// Create loads and installs a Chamber named chamberName.
func (a *Architect) Create(chamberName string) (*Chamber, error) {
	// Load the blueprint for the chamber.
	blueprint, err := a.LoadBlueprint(chamberName)
	if err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to load blueprint",
			err,
		)
	}

	// Create a chamber instance with the loaded blueprint.
	chamber := NewChamber(a.Burrow, chamberName, blueprint)

	// Resolve dependencies specified in the blueprint.
	dependencyDecorIDs := slices.Collect(maps.Keys(chamber.Blueprint))
	_ = dependencyDecorIDs
	err = chamber.Renovator.ResolveDependencies(dependencyDecorIDs)
	if err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to resolve direct dependencies",
			err,
		)
	}

	// Install decors after dependency resolution has produced a stable order.
	decorIDs := chamber.Blueprint.GetDecorIDs()
	if err := chamber.Renovator.ResolveDependencies(decorIDs); err != nil {
		return nil, err
	}
	orderedDecorIDs := chamber.Renovator.Plan.DependencyOrder
	err = chamber.InstallDecors(orderedDecorIDs)
	if err != nil {
		return nil, NewChamberError(
			chamberName,
			"failed to install decors",
			err,
		)
	}

	// Store the chamber in the map.
	a.ChambersByNames[chamberName] = chamber

	return chamber, nil
}

// Delete uninstalls and removes the Chamber named chamberName.
func (a *Architect) Delete(chamberName string) error {
	chamber, exists := a.ChambersByNames[chamberName]
	if !exists {
		return NewChamberError(
			chamberName,
			"failed to delete chamber because it is not created",
			nil,
		)
	}

	// Uninstall decors.
	orderedDecorIDs := slices.Clone(chamber.Renovator.Plan.DependencyOrder)
	slices.Reverse(orderedDecorIDs)

	if err := chamber.UninstallDecors(orderedDecorIDs); err != nil {
		return NewChamberError(chamberName, "failed to uninstall decors", err)
	}

	// Update and save the chamber blueprint.
	if err := UpdateBlueprint(chamber); err != nil {
		return NewChamberError(chamberName, "failed to update blueprint", err)
	}
	chamber.Blueprint.SaveToTomlFile(a.GetBlueprintPath(chamberName))

	// Remove the chamber from the map.
	delete(a.ChambersByNames, chamberName)

	return nil
}

// GetChamber returns the Chamber named chamberName.
func (a *Architect) GetChamber(chamberName string) (*Chamber, error) {
	chamber, exists := a.ChambersByNames[chamberName]
	if !exists {
		return nil, NewChamberError(
			chamberName,
			"failed to get chamber because it is not created",
			nil,
		)
	}

	return chamber, nil
}

// GetChamberOrCreate returns the Chamber named chamberName, creating it when
// needed.
func (a *Architect) GetChamberOrCreate(chamberName string) (*Chamber, error) {
	chamber, err := a.GetChamber(chamberName)
	if err != nil {
		return a.Create(chamberName)
	}

	return chamber, nil
}
