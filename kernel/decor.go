package kernel

// Decor is the instance that is installed in the Chamber.
type Decor interface {
	Chamber() *Chamber
	RawSpec() RawSpec
	Dependencies() []string

	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}

// TypedDecor is a generic implementation of Decor that can be used for any type
// of specification.
type TypedDecor[S any] struct {
	// chamber is the Chamber this decoration is installed in.
	chamber *Chamber

	// dependencies is the dependencies of this decor.
	dependencies []string

	// Spec configures this decoration.
	Spec *S

	// BuildRawSpec builds the RawSpec for this decor.
	BuildRawSpec func() (RawSpec, error)

	OnAssemble    func() error
	OnLaunch      func() error
	OnTerminate   func() error
	OnDisassemble func() error
}

// NewDecor creates a new TypedDecor with the given specification and
// dependencies.
func NewDecor[S any](
	chamber *Chamber,
	spec *S,
	dependencies []string,
) *TypedDecor[S] {
	return &TypedDecor[S]{
		chamber: chamber, Spec: spec, dependencies: dependencies,
		BuildRawSpec: func() (RawSpec, error) {
			return RawSpec{}, nil
		},
	}
}

// Chamber returns the Chamber this decoration is installed in.
func (d *TypedDecor[S]) Chamber() *Chamber {
	return d.chamber
}

// RawSpec returns the RawSpec for this decor. If BuildRawSpec is not nil, it
// will be used to build the RawSpec. Otherwise, an empty RawSpec will be
// returned.
func (d *TypedDecor[S]) RawSpec() RawSpec {
	if d.BuildRawSpec != nil {
		raw, err := d.BuildRawSpec()
		if err != nil {
			return RawSpec{}
		}
		return raw
	}

	return RawSpec{}
}

// Dependencies returns the dependencies of this decor.
func (d *TypedDecor[S]) Dependencies() []string {
	return d.dependencies
}

// Assemble calls the OnAssemble function if it is not nil. Otherwise, it does
// nothing.
func (d *TypedDecor[S]) Assemble() error {
	if d.OnAssemble != nil {
		return d.OnAssemble()
	}

	return nil
}

// Launch calls the OnLaunch function if it is not nil. Otherwise, it does
// nothing.
func (d *TypedDecor[S]) Launch() error {
	if d.OnLaunch != nil {
		return d.OnLaunch()
	}

	return nil
}

// Terminate calls the OnTerminate function if it is not nil. Otherwise, it does
// nothing.
func (d *TypedDecor[S]) Terminate() error {
	if d.OnTerminate != nil {
		return d.OnTerminate()
	}

	return nil
}

// Disassemble calls the OnDisassemble function if it is not nil. Otherwise, it
// does nothing.
func (d *TypedDecor[S]) Disassemble() error {
	if d.OnDisassemble != nil {
		return d.OnDisassemble()
	}

	return nil
}
