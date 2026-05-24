package kernel

import (
	"fmt"
	"reflect"
)

// Renovator resolves and manages chamber decors.
type Renovator struct {
	// Chamber owns the Renovator.
	Chamber *Chamber

	// DecorsByIDs stores resolved decor instances by decor ID.
	DecorsByIDs map[string]IDecor

	// DecorsByTypes stores resolved decor instances by concrete type.
	DecorsByTypes map[reflect.Type]IDecor

	// Plan is the most recently resolved dependency plan.
	Plan *Plan
}

// NewRenovator returns a Renovator for chamber.
func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		Chamber:       chamber,
		DecorsByIDs:   make(map[string]IDecor),
		DecorsByTypes: make(map[reflect.Type]IDecor),
		Plan:          nil,
	}
}

// ResolveDependencies resolves decorIDs into a Plan and decor lookup maps.
func (r *Renovator) ResolveDependencies(decorIDs []string) error {
	plan := NewPlan()
	if err := plan.CreatePlan(r, decorIDs); err != nil {
		return r.Chamber.Error(
			"failed to create plan for resolving dependencies",
			err,
		)
	}

	r.Plan = plan

	r.DecorsByIDs = make(map[string]IDecor)
	r.DecorsByTypes = make(map[reflect.Type]IDecor)
	for _, decorID := range plan.DependencyOrder {
		decor := plan.PlanNodesByDecorIDs[decorID].Decor
		r.DecorsByIDs[decorID] = decor
		r.DecorsByTypes[reflect.TypeOf(decor)] = decor
	}

	return nil
}

// CreateDecor creates the decor identified by decorID with spec.
func (r *Renovator) CreateDecor(
	decorID string,
	spec Vars,
) (IDecor, *DecorDef, error) {
	chamber := r.Chamber

	decorDef, exists := chamber.Burrow.Warehouse.DecorDefsByIDs[decorID]
	if !exists {
		return nil, nil, chamber.Error(
			fmt.Sprintf("definition for decor %q does not exist", decorID),
			nil,
		)
	}

	createFunc := decorDef.CreateFunc
	if createFunc == nil {
		return nil, nil, chamber.Error(
			fmt.Sprintf("decor %q does not have a create function", decorID),
			nil,
		)
	}

	decor, err := createFunc(r.Chamber, spec)
	if err != nil {
		return nil, decorDef, chamber.Error(
			fmt.Sprintf("failed to create decor %q", decorID),
			err,
		)
	}

	return decor, decorDef, nil
}

// GetDecorByID returns the decor identified by decorID.
func (r *Renovator) GetDecorByID(decorID string) (IDecor, error) {
	decor, exists := r.DecorsByIDs[decorID]
	if !exists {
		return nil, r.Chamber.Error(
			fmt.Sprintf("decor with ID %q does not exist", decorID),
			nil,
		)
	}

	return decor, nil
}

// GetDecorByType returns the decor registered for decorType.
func (r *Renovator) GetDecorByType(decorType reflect.Type) (IDecor, error) {
	decor, exists := r.DecorsByTypes[decorType]
	if !exists {
		return nil, r.Chamber.Error(
			fmt.Sprintf("decor with type %q does not exist", decorType),
			nil,
		)
	}

	return decor, nil
}

// GetDecorDependencyOrder returns resolved decors with dependencies first.
func (r *Renovator) GetDecorDependencyOrder() ([]IDecor, error) {
	if r.Plan == nil {
		return nil, r.Chamber.Error(
			"cannot get decor dependency order before resolving dependencies",
			nil,
		)
	}

	decorDependencyOrder := []IDecor{}
	for _, decorID := range r.Plan.DependencyOrder {
		decorDependencyOrder = append(
			decorDependencyOrder,
			r.DecorsByIDs[decorID],
		)
	}

	return decorDependencyOrder, nil
}

// AssmbleDecors assembles decors in order.
func (r *Renovator) AssmbleDecors(decors []IDecor) error {
	for _, decor := range decors {
		if err := decor.Assemble(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to assemble decor of type %q",
					reflect.TypeFor[*Decor](),
				),
				err,
			)
		}
	}

	return nil
}

// LaunchDecors launches decors in order.
func (r *Renovator) LaunchDecors(decors []IDecor) error {
	for _, decor := range decors {
		if err := decor.Launch(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to launch decor of type %q",
					reflect.TypeFor[*Decor](),
				),
				err,
			)
		}
	}

	return nil
}

// TerminateDecors terminates decors in order.
func (r *Renovator) TerminateDecors(decors []IDecor) error {
	for _, decor := range decors {
		if err := decor.Launch(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to terminate decor of type %q",
					reflect.TypeFor[*Decor](),
				),
				err,
			)
		}
	}

	return nil
}

// DisassembleDecors disassembles decors in order.
func (r *Renovator) DisassembleDecors(decors []IDecor) error {
	for _, decor := range decors {
		if err := decor.Launch(); err != nil {
			return r.Chamber.Error(
				fmt.Sprintf(
					"failed to disassemble decor of type %q",
					reflect.TypeFor[*Decor](),
				),
				err,
			)
		}
	}

	return nil
}

// InstallDecors assembles and launches resolved decors.
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

// UninstallDecors terminates and disassembles resolved decors.
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
