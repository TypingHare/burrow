package kernel

import "fmt"

// DecorDef defines the interface for a decor definition, which includes methods
// for parsing a raw specification and building a decor instance.
type DecorDef interface {
	Name() string
	Parse(RawSpec) (any, error)
	Build(*Chamber, any) (Decor, error)
}

// TypedDecorDef is a generic implementation of DecorDef that uses a specific
// type for the decor specification. It includes a name, a parsing function, and
// a building function that operates on the typed specification.
type TypedDecorDef[S any] struct {
	name       string
	ParseSpec  func(RawSpec) (*S, error)
	BuildDecor func(*Chamber, *S) (Decor, error)
}

// NewTypedDecorDef creates a new TypedDecorDef with the given name, parsing
// function, and building function.
func NewTypedDecorDef[S any](
	name string,
	parse func(RawSpec) (*S, error),
	build func(*Chamber, *S) (Decor, error),
) *TypedDecorDef[S] {
	return &TypedDecorDef[S]{
		name:       name,
		ParseSpec:  parse,
		BuildDecor: build,
	}
}

// Name returns the name of the decor definition.
func (d *TypedDecorDef[S]) Name() string {
	return d.name
}

// Parse uses the provided ParseSpec function to parse the raw specification
// into the typed specification. It returns the parsed specification as an any
// type and any error that occurs during parsing.
func (d *TypedDecorDef[S]) Parse(raw RawSpec) (any, error) {
	return d.ParseSpec(raw)
}

// Build uses the provided BuildDecor function to build a decor instance from
// the typed specification. It first checks if the provided spec is of the
// correct type and returns an error if it is not. If the type is correct, it
// calls the BuildDecor function to create the decor instance and returns it
// along with any error that occurs during building.
func (d *TypedDecorDef[S]) Build(
	chamber *Chamber,
	spec any,
) (Decor, error) {
	typed, ok := spec.(*S)
	if !ok {
		return nil, fmt.Errorf("invalid spec type for decor %q", d.name)
	}

	return d.BuildDecor(chamber, typed)
}
