package kernel

import "fmt"

const (
	// MetadataName is the carton metadata key for the carton name.
	MetadataName = "NAME"

	// MetadataVersion is the carton metadata key for the carton version.
	MetadataVersion = "VERSION"
)

// Carton contains metadata and the decor definitions it exposes.
type Carton struct {
	// Metadata stores carton metadata.
	Metadata Vars

	// DecorDefsByNames stores decor definitions by name.
	DecorDefsByNames map[string]*DecorDef
}

// NewCarton returns a Carton with name and version metadata.
func NewCarton(name string, version string) *Carton {
	carton := &Carton{
		Metadata:         NewVars(),
		DecorDefsByNames: make(map[string]*DecorDef),
	}

	carton.Metadata.Set(MetadataName, name)
	carton.Metadata.Set(MetadataVersion, version)

	return carton
}

// Name returns the carton name.
func (c *Carton) Name() string {
	return c.Metadata.Get(MetadataName)
}

// Version returns the carton version.
func (c *Carton) Version() string {
	return c.Metadata.Get(MetadataVersion)
}

// AddDecorDef registers decorDef with the carton.
func (c *Carton) AddDecorDef(decorDef *DecorDef) error {
	decorName := decorDef.Name
	if decorName == "" {
		return fmt.Errorf("decor name cannot be empty")
	}

	if decorDef == nil {
		return fmt.Errorf("decor definition with name %q is nil", decorName)
	}

	if _, exists := c.DecorDefsByNames[decorName]; exists {
		return fmt.Errorf(
			"decor definition with name %q already exists",
			decorName,
		)
	}

	c.DecorDefsByNames[decorName] = decorDef

	return nil
}
