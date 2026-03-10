package kernel

type Decor interface {
	Chamber() *Chamber
	RawSpec() RawSpec
	Dependencies() []string

	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}

type TypedDecor[S any] struct {
	// chamber is the Chamber this decoration is installed in.
	chamber *Chamber

	// Spec configures this decoration.
	Spec *S

	// Deps is the dependencies of this decor.
	Deps []string

	// BuildRawSpec builds the RawSpec for this decor.
	BuildRawSpec func() (RawSpec, error)

	OnAssemble    func() error
	OnLaunch      func() error
	OnTerminate   func() error
	OnDisassemble func() error
}

func NewDecor[S any](chamber *Chamber, spec *S, deps []string) *TypedDecor[S] {
	return &TypedDecor[S]{
		chamber: chamber, Spec: spec, Deps: deps,
		BuildRawSpec: func() (RawSpec, error) {
			return RawSpec{}, nil
		},
	}
}

func (d *TypedDecor[S]) Chamber() *Chamber {
	return d.chamber
}

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

func (d *TypedDecor[S]) Dependencies() []string {
	return d.Deps
}

func (d *TypedDecor[S]) Assemble() error {
	if d.OnAssemble != nil {
		return d.OnAssemble()
	}

	return nil
}

func (d *TypedDecor[S]) Launch() error {
	if d.OnLaunch != nil {
		return d.OnLaunch()
	}

	return nil
}

func (d *TypedDecor[S]) Terminate() error {
	if d.OnTerminate != nil {
		return d.OnTerminate()
	}

	return nil
}

func (d *TypedDecor[S]) Disassemble() error {
	if d.OnDisassemble != nil {
		return d.OnDisassemble()
	}

	return nil
}
