package kernel

import "fmt"

const (
	MetadataGoModule = "GO_MODULE"
	MetadataName     = "NAME"
	MetadataVersion  = "VERSION"
	MetadataAuthor   = "AUTHOR"
	MetadataEmail    = "EMAIL"
)

// Carton contains a collection of decoration factories.
type Carton struct {
	// Metadata is the metadata of this carton, which is used to store
	// information about the carton. For simplicity, we use Vars to store the
	// metadata.
	Metadata Vars

	// decorationFactoryMap is a map of decoration names to corresponding
	// factories.
	decorationFactoryMap map[string]DecorationFactory
}

// NewCarton creates a new carton with the given carton directory.
func NewCarton() *Carton {
	return &Carton{
		Metadata:             NewVars(),
		decorationFactoryMap: make(map[string]DecorationFactory),
	}
}

// AddDecorationFactory adds a decoration factory with the given name.
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
			"decoration factory with name '%s' already exists",
			name,
		)
	}

	c.decorationFactoryMap[name] = factory

	return nil
}

// AddTypedDecorationFactory adds a typed decoration factory with the given
// name, SpecParser, and DecorationBuilder.
func AddTypedDecorationFactory[S any](
	c *Carton,
	name string,
	parse SpecParser[S],
	build DecorationBuilder[S],
) error {
	return c.AddDecorationFactory(
		name,
		func(ch *Chamber, raw RawSpec) (DecorationInstance, error) {
			spec, err := parse(raw)
			if err != nil {
				return nil, fmt.Errorf("invalid %s spec: %w", name, err)
			}

			decoration, err := build(ch, spec)
			if err != nil {
				return nil, err
			}
			if decoration == nil {
				return nil, fmt.Errorf(
					"build %s decoration: nil decoration",
					name,
				)
			}

			return decoration, nil
		},
	)
}
