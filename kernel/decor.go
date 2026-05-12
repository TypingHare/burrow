package kernel

// IDecor defines the lifecycle contract for a chamber decor.
type IDecor interface {
	Chamber() *Chamber
	Spec() Vars
	Dependencies() []string
	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}

// Decor is a chamber extension with optional lifecycle hooks.
type Decor struct {
	// chamber owns the decor instance.
	chamber *Chamber

	// spec holds configuration values used to create the decor.
	spec Vars

	// dependencies lists decor IDs that must run before this decor.
	dependencies []string

	// AssembleFunc runs before the decor is launched.
	AssembleFunc func() error

	// LaunchFunc starts the decor after it has been assembled.
	LaunchFunc func() error

	// TerminateFunc stops the decor before it is disassembled.
	TerminateFunc func() error

	// DisassembleFunc runs after the decor has been terminated.
	DisassembleFunc func() error
}

// NewDecor returns a Decor for chamber with spec.
func NewDecor(chamber *Chamber, spec Vars) *Decor {
	return &Decor{
		chamber:      chamber,
		spec:         spec,
		dependencies: []string{},

		AssembleFunc:    nil,
		LaunchFunc:      nil,
		TerminateFunc:   nil,
		DisassembleFunc: nil,
	}
}

// Chamber returns the Chamber that owns the Decor.
func (d *Decor) Chamber() *Chamber {
	return d.chamber
}

// Spec returns the decor's configuration values.
func (d *Decor) Spec() Vars {
	return d.spec
}

// Dependencies returns the IDs of decors that must be installed before this
// decor.
func (d *Decor) Dependencies() []string {
	return d.dependencies
}

// Assemble runs the decor's assemble hook, if one is configured.
func (d *Decor) Assemble() error {
	if d.AssembleFunc != nil {
		return d.AssembleFunc()
	}

	return nil
}

// Launch runs the decor's launch hook, if one is configured.
func (d *Decor) Launch() error {
	if d.LaunchFunc != nil {
		return d.LaunchFunc()
	}

	return nil
}

// Terminate runs the decor's terminate hook, if one is configured.
func (d *Decor) Terminate() error {
	if d.TerminateFunc != nil {
		return d.TerminateFunc()
	}

	return nil
}

// Disassemble runs the decor's disassemble hook, if one is configured.
func (d *Decor) Disassemble() error {
	if d.DisassembleFunc != nil {
		return d.DisassembleFunc()
	}

	return nil
}
