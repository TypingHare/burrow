package kernel

type Decor[S any] struct {
	// Chamber is the Chamber this decoration is installed in.
	Chamber *Chamber

	// Spec configures this decoration.
	Spec *S

	// Deps is the dependencies of this decor.
	Deps []string

	OnAssemble    func() error
	OnLaunch      func() error
	OnTerminate   func() error
	OnDisassemble func() error
}

func NewDecor[S any](chamber *Chamber, spec *S, deps []string) *Decor[S] {
	return &Decor[S]{Chamber: chamber, Spec: spec, Deps: deps}
}

func (d *Decor[S]) Assemble() error {
	if d.OnAssemble != nil {
		return d.OnAssemble()
	}

	return nil
}

func (d *Decor[S]) Launch() error {
	if d.OnLaunch != nil {
		return d.OnLaunch()
	}

	return nil
}

func (d *Decor[S]) Terminate() error {
	if d.OnTerminate != nil {
		return d.OnTerminate()
	}

	return nil
}

func (d *Decor[S]) Disassemble() error {
	if d.OnDisassemble != nil {
		return d.OnDisassemble()
	}

	return nil
}
