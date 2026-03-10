### Decor Directory Structure

The directory structure of a decor is organized to promote separation of concerns and maintainability. Below is a recommended structure for a decor named "demo":

```bash
demo                    # Root directory of the decor
├── api                 # API layer of the decor
├── command             # Command layer of the decor
│   └── list.go         # An example command factory file
├── share               # Shared entities and utilities
│   ├── spec.go         # Decoration specification definitions
│   ├── contract.go     # Contract definitions for the decor
│   └── demo            # An example shared module
└── decor.go            # Main entry point for the decor
```

The `decor.go` file serves as the main entry point for the decor, where the typed decor is defined. It should contain a decor embedding the `kernel.TypedDecor` with a specific specification type defined in the `share/spec.go` file:

```go
// decor.go
type DemoTypedDecor struct {
	kernel.TypedDecor[share.DemoSpec]
}

function Foo() string {
    return "foo"
}
```

It should also include the definition of the typed decor using `kernel.NewTypedDecorDef`, which specifies the name of the decor, a function to create a new specification, and a function to create a new decor instance:

```go
// decor.go
var DemoTypedDecorDef = kernel.NewTypedDecorDef[share.DemoSpec](
    "demo",
    share.NewDemoSpec,
    func(c *kernel.Chamber, r *share.DemoSpec) (*kernel.DemoTypedDecor, error) {
        return &share.DemoTypedDecor{}, nil
    },
)
```

In the `share/spec.go` file, you should define both the `DemoSpec` struct and a `NewDemoSpec` function that returns a new instance of `DemoSpec`:

```go
// share/spec.go
type DemoSpec struct {
    // Define fields for the specification here
}

func NewDemoSpec(r kernel.RawSpec) (*kernel.DemoSpec, error) {
    return &share.DemoSpec{}, nil
},
```

In the `decor.go` file, you should also define a `UseDecor` function that allows the decor to be used within a chamber and a function that registers the decor itself to a carton:

```go
// decor.go
func UseTypedDecor(c *kernel.Chamber) (*DemoTypedDecor, error) {
    return kernel.UseTypedDecor[DemoTypedDecor](c)
}

func RegisterToCarton(c *kernel.Carton) {
    c.RegisterDecor(DemoTypedDecorDef)
}
```

The `share/contract.go` should contain contract definitions (interfaces) that the decor will implement. It must contain a `DemoDecor` interface that defines the methods the decor will provide:

```go
// share/contract.go
type DemoDecor interface {
    kernel.Decor
    Foo() string
}
```

This interface is used around the API layer, the command layer, and the share modules to address the circular import issue, because in the `decor.go` file, the lifecycle functions may need to call command factory functions in the command layer.

The **share modules** refer to the directories in the `share` directory, which contain shared entities and utilities for the decor. They can be used by the API layer, the command layer, and other share modules. For instance, `share/demo` is a share module in the decor. It is recommended to have a share module named after the decor itself, which contains entities and utilities that may be used by other decors.

The **API layer** refers to the `api` directory, which contains the API definitions for the decor. They are usually used by the command layer, but can also be used by other decors.

The **command layer** refers to the `command` directory, which contains command factory functions for the decor. The commands are evaluated by these functions and added to the root command in the `OnAssemble` function of the decor.

Note that the API layer and the command layer are optional. Some decors may not register any commands, and they don't need to have these two layers.
