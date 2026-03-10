package kernel

import "fmt"

type Renovator struct {
	chamber     *Chamber
	decorsByIDs map[string]DecorInstance
}

func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		chamber:     chamber,
		decorsByIDs: make(map[string]DecorInstance),
	}
}

func (r *Renovator) Chamber() *Chamber {
	return r.chamber
}

func (r *Renovator) DecorsByIDs() map[string]DecorInstance {
	return r.decorsByIDs
}

func (r *Renovator) GetDecorByID(decorID string) (DecorInstance, error) {
	decor, exists := r.decorsByIDs[decorID]
	if !exists {
		return nil, fmt.Errorf("decor with ID %s not found", decorID)
	}

	return decor, nil
}
