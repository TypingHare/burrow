package kernel

import (
	"fmt"
	"reflect"
)

// Renovator is responsible for resolving direct dependencies of a chamber.
type Renovator struct {
	Chamber       *Chamber
	DecorsByIDs   map[string]Decor
	DecorsByTypes map[reflect.Type]Decor
	Plan          *Plan
}

// NewRenovator creates a new instance of Renovator for the given chamber.
func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		Chamber:       chamber,
		DecorsByIDs:   make(map[string]Decor),
		DecorsByTypes: make(map[reflect.Type]Decor),
		Plan:          nil,
	}
}

func (r *Renovator) CreateDecor(
	decorID string,
	rawSpec RawSpec,
) (Decor, DecorDef, error) {
	chamber := r.Chamber
	decorDef, exists := chamber.Burrow.Warehouse.DecorDefsByIDs[decorID]
	if !exists {
		return nil, nil, chamber.Error(
			fmt.Sprintf("definition for decor %q does not exist", decorID),
			nil,
		)
	}

	spec, err := decorDef.Parse(rawSpec)
	if err != nil {
		return nil, decorDef, chamber.Error(
			fmt.Sprintf("failed to parse spec for decor %q", decorID),
			err,
		)
	}

	decor, err := decorDef.Build(chamber, spec)
	if err != nil {
		return nil, decorDef, chamber.Error(
			fmt.Sprintf("failed to build decor %q", decorID),
			err,
		)
	}

	return decor, decorDef, nil
}

func (r *Renovator) ResolveDependencies(decorIDs []string) error {
	plan := NewPlan()
	if err := plan.CreatePlan(r.Chamber, decorIDs); err != nil {
		return r.Chamber.Error(
			"failed to create plan for resolving dependencies",
			err,
		)
	}

	r.Plan = plan

	r.DecorsByIDs = make(map[string]Decor)
	r.DecorsByTypes = make(map[reflect.Type]Decor)
	for _, decorID := range plan.DependencyOrder {
		decor := plan.PlanNodesByDecorIDs[decorID].Decor
		r.DecorsByIDs[decorID] = decor
		r.DecorsByTypes[reflect.TypeOf(decor)] = decor
	}

	return nil
}

func (r *Renovator) GetDecorByID(decorID string) (Decor, error) {
	decor, exists := r.DecorsByIDs[decorID]
	if !exists {
		return nil, r.Chamber.Error(
			fmt.Sprintf("decor with ID %q does not exist", decorID),
			nil,
		)
	}

	return decor, nil
}

func (r *Renovator) GetDecorByType(decorType reflect.Type) (Decor, error) {
	decor, exists := r.DecorsByTypes[decorType]
	if !exists {
		return nil, r.Chamber.Error(
			fmt.Sprintf("decor with type %q does not exist", decorType),
			nil,
		)
	}

	return decor, nil
}

func (r *Renovator) GetDecorDependencyOrder() ([]Decor, error) {
	if r.Plan == nil {
		return nil, r.Chamber.Error(
			"cannot get decor dependency order before resolving dependencies",
			nil,
		)
	}

	decorDependencyOrder := []Decor{}
	for _, decorID := range r.Plan.DependencyOrder {
		decorDependencyOrder = append(
			decorDependencyOrder,
			r.DecorsByIDs[decorID],
		)
	}

	return decorDependencyOrder, nil
}

func (r *Renovator) AssmbleDecors(decors []Decor) error {
	for _, decor := range decors {
		if err := decor.Assemble(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to assemble decor of type %q",
					reflect.TypeOf(decor),
				),
				err,
			)
		}
	}

	return nil
}

func (r *Renovator) LaunchDecors(decors []Decor) error {
	for _, decor := range decors {
		if err := decor.Launch(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to launch decor of type %q",
					reflect.TypeOf(decor),
				),
				err,
			)
		}
	}

	return nil
}

func (r *Renovator) TerminateDecors(decors []Decor) error {
	for _, decor := range decors {
		if err := decor.Launch(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to terminate decor of type %q",
					reflect.TypeOf(decor),
				),
				err,
			)
		}
	}

	return nil
}

func (r *Renovator) DisassembleDecors(decors []Decor) error {
	for _, decor := range decors {
		if err := decor.Launch(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to disassemble decor of type %q",
					reflect.TypeOf(decor),
				),
				err,
			)
		}
	}

	return nil
}

func (r *Renovator) InstallDecors() error {
	decorDependencyOrder, err := r.GetDecorDependencyOrder()
	if err != nil {
		return r.Chamber.Error(
			"failed to get decor dependency order for installation",
			err,
		)
	}

	r.AssmbleDecors(decorDependencyOrder)
	r.LaunchDecors(decorDependencyOrder)

	return nil
}

func (r *Renovator) UninstallDecors() error {
	decorDependencyOrder, err := r.GetDecorDependencyOrder()
	if err != nil {
		return r.Chamber.Error(
			"failed to get decor dependency order for uninstallation",
			err,
		)
	}

	r.TerminateDecors(decorDependencyOrder)
	r.DisassembleDecors(decorDependencyOrder)

	return nil
}
