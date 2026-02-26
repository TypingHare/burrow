package kernel

import "fmt"

type DecorationMap = map[string]DecorationInstance

// Renovator is responsible for managing decorations in a chamber.
type Renovator struct {
	// Chamber is the chamber that the renovator is managing.
	chamber *Chamber

	// DecorationMap is a map of decoration IDs to their corresponding
	// decorations.
	decorationMap DecorationMap
}

// NewRenovator creates a new Renovator for the given chamber.
func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		chamber:       chamber,
		decorationMap: make(DecorationMap),
	}
}

// Decorations returns the map of decoration IDs to their corresponding
// decorations.
func (r *Renovator) Decorations() DecorationMap {
	return r.decorationMap
}

// GetDecoration returns the Decoration instance for the given decoration ID.
func (r *Renovator) GetDecoration(
	decorationID string,
) (DecorationInstance, bool) {
	decoration, ok := r.decorationMap[decorationID]
	return decoration, ok
}

// AddDecoration adds a decoration to the chamber and returns the instance of
// the decoration. It takes the decoration ID and the raw specification as
// arguments. If the decoration factory for the given ID is not found or if
// there is an error creating the decoration, it returns an error.
func (r *Renovator) AddDecoration(
	decorationID string,
	rawSpec RawSpec,
) (DecorationInstance, error) {
	decorationFactory, err := r.chamber.Burrow().
		Warehouse().
		GetDecorationFactory(decorationID)
	if err != nil {
		return nil, NewChamberError(
			r.chamber.Name(),
			fmt.Sprintf("get decoration for dependency '%s'", decorationID),
			err,
		)
	}

	decoration, err := decorationFactory(r.chamber, rawSpec)
	if err != nil {
		return nil, NewChamberError(
			r.chamber.Name(),
			fmt.Sprintf("create decoration for dependency '%s'", decorationID),
			err,
		)
	}

	r.decorationMap[decorationID] = decoration

	return decoration, nil
}
