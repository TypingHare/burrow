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

// NewCarton creates a new carton with the given name and version.
func NewCarton(name string, version string) *Carton {
	carton := &Carton{
		Metadata:         NewVars(),
		DecorDefsByNames: make(map[string]DecorDef),
	}

	carton.Metadata.Set(MetadataName, name)
	carton.Metadata.Set(MetadataVersion, version)

	return carton
}

func (c *Carton) Name() string {
	return c.Metadata.Get(MetadataName)
}

func (c *Carton) Version() string {
	return c.Metadata.Get(MetadataVersion)
}

func (c *Carton) AddDecorDef(decorName string, decorDef DecorDef) error {
	if decorName == "" {
		return fmt.Errorf("decor name cannot be empty")
	}

	if decorDef == nil {
		return fmt.Errorf("decor definition cannot be nil")
	}

	if _, exists := c.DecorDefsByNames[decorName]; exists {
		return fmt.Errorf(
			"decor definition with name '%q' already exists",
			decorName,
		)
	}

	c.DecorDefsByNames[decorName] = decorDef

	return nil
}

func AddTypedDecorDef[S any](
	c *Carton,
	decorName string,
	parse func(RawSpec) (*S, error),
	build func(*Chamber, *S) (Decor, error),
) error {
	typedDecorDef := NewTypedDecorDef(decorName, parse, build)
	c.AddDecorDef(decorName, typedDecorDef)

	return nil
}
