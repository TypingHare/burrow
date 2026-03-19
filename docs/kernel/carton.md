### Carton

A **carton** is a container that holds a set of **decor definitions** (see [decor](./decor.md)) and some metadata.

```go
// kernel/carton.go
type Carton struct {
	Metadata Vars
	DecorDefsByNames map[string]DecorDef
}
```

Carton metadata is a set of attributes related to the carton, which allows the kernel and other decorations to make some decisions. There are two important attributes that are used by the kernel:

```go
// kernel/carton.go
const (
	MetadataName    = "NAME"
	MetadataVersion = "VERSION"
)
```

Kernel defines two methods to get these two attributes:

```go
// kernel/carton.go
func (c *Carton) Name() string {
	return c.Metadata.Get(MetadataName)
}

func (c *Carton) Version() string {
	return c.Metadata.Get(MetadataVersion)
}
```

Always use `NewCarton` to create a carton:

```go
// kernel/carton.go
carton := kernel.NewCarton("github.com/yourname/your-carton.carton", "2026.1.1")
```

Note that the major and minor version numbers of the carton should be the same as the kernel version that the carton is compatible with. See more details in [version](./version.md).

#### Add Decor Definitions to Carton

Always use `AddTypedDecorDef` to add a decor definition to a carton:

```go
// kernel/carton.go
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
```

Because `TypedDecorDef` is an implementation of `DecorDef`, it can be added to the carton by calling `AddDecorDef`.
