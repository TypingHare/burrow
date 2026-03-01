package kernel

// DecorationInstance represents an installed decoration and its lifecycle.
type DecorationInstance interface {
	Chamber() *Chamber
	Dependencies() []string

	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}

// Decoration is a generic base type for decoration implementations.
type Decoration[S any] struct {
	// chamber is the Chamber the decoration is installed in.
	chamber *Chamber

	// spec configures the decoration.
	spec S
}

// NewDecoration returns a Decoration for chamber and spec.
func NewDecoration[S any](chamber *Chamber, spec S) *Decoration[S] {
	return &Decoration[S]{
		chamber: chamber,
		spec:    spec,
	}
}

// Chamber returns the Chamber the decoration is installed in.
func (d *Decoration[S]) Chamber() *Chamber {
	return d.chamber
}

// Spec returns the decoration's specification.
func (d *Decoration[S]) Spec() S {
	return d.spec
}

// SpecParser converts a RawSpec to a typed specification.
type SpecParser[S any] func(rawSpec RawSpec) (S, error)

// DecorationBuilder builds a DecorationInstance from a Chamber and typed spec.
type DecorationBuilder[S any] func(*Chamber, S) (DecorationInstance, error)

// DecorationFactory builds a DecorationInstance from a Chamber and RawSpec.
type DecorationFactory func(
	chamber *Chamber,
	rawSpec RawSpec,
) (DecorationInstance, error)
