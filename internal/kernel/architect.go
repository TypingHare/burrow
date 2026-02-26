package kernel

// Architect is responsible for managing the chambers of the burrow.
type Architect struct {
	// burrow is the Burrow that this Architect manages.
	burrow *Burrow

	// chamberMap maps chamber names to their corresponding Chamber instances.
	chamberMap map[string]*Chamber
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

// ChamberMap returns the map of chamber names to their corresponding ChamberMap
// instances.
func (a *Architect) ChamberMap() map[string]*Chamber {
	return a.chamberMap
}
