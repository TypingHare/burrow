package kernel

// ChamberError is an error that occurs when a Chamber operation fails.
type ChamberError struct {
	// ChamberName is the name of the Chamber that the error occurred in.
	ChamberName string

	// Op is the operation that was being performed when the error occurred.
	Op string

	// Err is the underlying error that caused the ChamberError.
	Err error
}

// NewChamberError creates a new ChamberError with the given chamber name,
// operation, and underlying error.
func NewChamberError(chamberName, op string, err error) *ChamberError {
	return &ChamberError{
		ChamberName: chamberName,
		Op:          op,
		Err:         err,
	}
}

// Error returns a string representation of the ChamberError.
func (e *ChamberError) Error() string {
	return e.ChamberName + " " + e.Op + ": " + e.Err.Error()
}

// Unwrap returns the underlying error that caused the ChamberError.
func (e *ChamberError) Unwrap() error { return e.Err }

// Chamber is an independent CLI application inside the Burrow.
type Chamber struct {
	// burrow is the Burrow that this Chamber belongs to.
	burrow *Burrow

	// name is the name of the Chamber.
	name string

	// blueprint is the Blueprint that defines the configuration of the Chamber.
	blueprint Blueprint
}

// NewChamber creates a new Chamber for the given Burrow.
func NewChamber(burrow *Burrow, name string, blueprint Blueprint) *Chamber {
	return &Chamber{
		burrow:    burrow,
		name:      name,
		blueprint: blueprint,
	}
}

// Burrow returns the Burrow that this Chamber belongs to.
func (c *Chamber) Burrow() *Burrow {
	return c.burrow
}

// Name returns the name of the Chamber.
func (c *Chamber) Name() string {
	return c.name
}

// Blueprint returns the Blueprint of the Chamber.
func (c *Chamber) Blueprint() Blueprint {
	return c.blueprint
}

// Init initializes the Chamber by setting up its environment and dependencies.
func (c *Chamber) Init() error {
	return nil
}
