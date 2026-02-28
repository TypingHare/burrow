package kernel

import "fmt"

// Carton metadata keys.
const (
	MetadataName    = "NAME"
	MetadataVersion = "VERSION"
	MetadataAuthor  = "AUTHOR"
	MetadataEmail   = "EMAIL"
)

// Carton contains metadata and the decoration factories it exposes.
type Carton struct {
	// Metadata stores carton metadata.
	Metadata Vars

	// decorationFactoryMap stores decoration factories by name.
	decorationFactoryMap map[string]DecorationFactory
}

// NewCarton returns an empty Carton.
func NewCarton() *Carton {
	return &Carton{
		Metadata:             NewVars(),
		decorationFactoryMap: make(map[string]DecorationFactory),
	}
}

// AddDecorationFactory registers factory under name.
func (c *Carton) AddDecorationFactory(
	name string,
	factory DecorationFactory,
) error {
	if name == "" {
		return fmt.Errorf("decoration name cannot be empty")
	}

	if factory == nil {
		return fmt.Errorf("decoration factory cannot be nil")
	}

	if _, exists := c.decorationFactoryMap[name]; exists {
		return fmt.Errorf(
			"decoration factory with name '%q' already exists",
			name,
		)
	}

	c.decorationFactoryMap[name] = factory

	return nil
}

// AddTypedDecorationFactory registers a typed decoration factory.
//
// It parses rawSpec with parse, builds the decoration with build, and exposes
// the result through Carton's untyped factory interface.
func AddTypedDecorationFactory[S any](
	c *Carton,
	decorationName string,
	parse SpecParser[S],
	build DecorationBuilder[S],
) error {
	return c.AddDecorationFactory(
		decorationName,
		func(chamber *Chamber, rawSpec RawSpec) (DecorationInstance, error) {
			spec, err := parse(rawSpec)
			if err != nil {
				return nil, fmt.Errorf(
					"invalid raw spec for decoration %q: %w",
					decorationName,
					err,
				)
			}

			decoration, err := build(chamber, spec)
			if err != nil {
				return nil, fmt.Errorf(
					"failed to build decoration %q: %w",
					decorationName,
					err,
				)
			}
			if decoration == nil {
				return nil, fmt.Errorf(
					"failed to build decoration %q: decoration is nil",
					decorationName,
				)
			}

			return decoration, nil
		},
	)
}
