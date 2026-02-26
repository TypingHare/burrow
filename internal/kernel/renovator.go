package kernel

type DecorationMap = map[string]Decoration[any]

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
