package kernel

import "fmt"

// Carton metadata keys.
const (
	MetadataName    = "NAME"
	MetadataVersion = "VERSION"
)

// Carton contains metadata and the decoration factories it exposes.
type Carton struct {
	// Metadata stores carton metadata.
	Metadata Vars

	// DecorDefsByNames stores decoration factories by name.
	DecorDefsByNames map[string]DecorDef
}

// NewCarton returns an empty Carton.
func NewCarton() *Carton {
	return &Carton{
		Metadata:         NewVars(),
		DecorDefsByNames: make(map[string]DecorDef),
	}
}

func (c *Carton) AddDecorDef(name string, decorDef DecorDef) error {
	if name == "" {
		return fmt.Errorf("decor name cannot be empty")
	}

	if decorDef == nil {
		return fmt.Errorf("decor definition cannot be nil")
	}

	if _, exists := c.DecorDefsByNames[name]; exists {
		return fmt.Errorf(
			"decor definition with name '%q' already exists",
			name,
		)
	}

	c.DecorDefsByNames[name] = decorDef

	return nil
}

func (c *Carton) Name() string {
	return c.Metadata.Get(MetadataName)
}

func (c *Carton) Version() string {
	return c.Metadata.Get(MetadataVersion)
}

func AddTypedDecorDef[S any](
	c *Carton,
	decorName string,
	parse func(RawSpec) (*S, error),
	build func(*Chamber, *S) (Decor, error),
) error {
	typedDecorDef := NewTypedDecorDef(decorName, parse, build)
	c.DecorDefsByNames[decorName] = typedDecorDef

	return nil
}
