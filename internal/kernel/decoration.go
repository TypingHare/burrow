package kernel

type DecorationInstance interface {
	Chamber() *Chamber
	SpecAny() any
	Dependencies() []string

	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}

// Decoration is a component that can be installed to a Chamber and provides
// additional features.
type Decoration[S any] struct {
	// chamber is the chamber that the decoration is installed to.
	chamber *Chamber

	// spec is the specification of the decoration. It is used to configure the
	// decoration.
	spec S
}

// NewDecoration creates a new decoration with the given chamber, specification,
// and hooks.
func NewDecoration[S any](
	chamber *Chamber,
	spec S,
) *Decoration[S] {
	return &Decoration[S]{
		chamber: chamber,
		spec:    spec,
	}
}

// Chamber returns the chamber that the decoration is installed to.
func (d *Decoration[S]) Chamber() *Chamber {
	return d.chamber
}

// Spec returns the specification of the decoration.
func (d *Decoration[S]) Spec() S {
	return d.spec
}

// RawSpec is a raw specification that needs to be converted to a specific type
// before being used to configure a decoration.
type RawSpec = map[string]any

// SpecParser is a function that converts a RawSpec to a specific type.
type SpecParser[S any] func(rawSpec RawSpec) (S, error)

// DecorationBuilder is a function that creates a Decoration from a Chamber and
// a specific type specification.
type DecorationBuilder[S any] func(*Chamber, S) (DecorationInstance, error)

// DecorationFactory is a function that creates a DecorationHooks from a
// RawSpec.
type DecorationFactory func(
	chamber *Chamber,
	rawSpec RawSpec,
) (DecorationInstance, error)
