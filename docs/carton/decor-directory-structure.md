### Decor Directory Structure

There is no single required layout for a decor, but a consistent structure makes the code easier to grow and easier to share with other decors. A good default is to keep the decor type at the package root, put reusable types in `share`, and keep command or API code in their own directories when the decor needs them.

For a decor named `demo`, the directory usually looks like this:

```bash
demo
├── api
├── command
│   └── list.go
├── share
│   ├── contract.go
│   ├── spec.go
│   └── demo
└── decor.go
```

The important part is the separation of responsibilities:

- `decor.go` defines the decor type and its lifecycle hooks.
- `share/spec.go` defines the typed spec and the parser that converts `kernel.RawSpec` into that spec.
- `share/contract.go` defines interfaces that other packages can depend on without importing the concrete decor type.
- `api` contains API-facing types or helpers when the decor exposes an API to other decors.
- `command` contains command wiring when the decor adds CLI behavior.

The `api` and `command` directories are optional. Many decors only need `decor.go` and `share`.

#### `decor.go`

The root package should define the concrete decor type. In Burrow, that usually means embedding `kernel.TypedDecor[S]` with the decor's typed spec.

```go
package demo

import (
	"github.com/TypingHare/burrow/v2026/kernel"

	"github.com/TypingHare/burrow/demo/share"
)

type Decor struct {
	*kernel.TypedDecor[share.Spec]
}

func NewDecor(chamber *kernel.Chamber, spec *share.Spec) (kernel.Decor, error) {
	decor := &Decor{
		TypedDecor: kernel.NewDecor(chamber, spec, nil),
	}

	decor.OnAssemble = func() error {
		return nil
	}

	return decor, nil
}
```

Keep `decor.go` focused on the decor itself: construction, dependencies, lifecycle, and carton registration. If the decor starts collecting unrelated helpers, move them into `share`, `api`, or `command`.

#### `share/spec.go`

The spec parser belongs in `share/spec.go`. This file defines the typed configuration and the function that parses the raw spec.

```go
package share

import "github.com/TypingHare/burrow/v2026/kernel"

type Spec struct {
	Name string `json:"name"`
}

func NewSpec(raw kernel.RawSpec) (*Spec, error) {
	spec := &Spec{
		Name: "",
	}

	if name, ok := raw["name"].(string); ok {
		spec.Name = name
	}

	return spec, nil
}
```

The exact fields depend on the decor, but the pattern stays the same: define a typed struct, parse from `kernel.RawSpec`, and keep parsing logic close to the type it produces.

#### `share/contract.go`

`share/contract.go` is useful when other packages need to interact with the decor without importing its concrete implementation. That keeps package boundaries cleaner and helps avoid circular imports.

```go
package share

import "github.com/TypingHare/burrow/v2026/kernel"

type DemoDecor interface {
	kernel.Decor
	Foo() string
}
```

This contract is commonly used by the `api` and `command` packages, and by any shared subpackages under `share`.

#### `api` and `command`

Use `api` when the decor exposes reusable API-level helpers or types. Use `command` when the decor contributes commands to the chamber. Keeping those packages separate prevents `decor.go` from turning into a large grab bag of CLI code, shared types, and lifecycle logic.

The `command` package usually contains command factory functions. The decor can then call those functions from a lifecycle hook such as `OnAssemble` when it is time to register commands.

#### Exposing carton registration

The decor package should expose a small registration function so cartons can register it without knowing the details of its typed spec or builder.

```go
func RegisterToCarton(carton *kernel.Carton) error {
	return kernel.AddTypedDecorDef(
		carton,
		"demo",
		share.NewSpec,
		NewDecor,
	)
}
```

With that pattern, carton code stays simple:

```go
if err := demo.RegisterToCarton(carton); err != nil {
	return err
}
```

That separation is still useful when you organize the package: the decor owns its implementation and exposes the registration entry point, while the carton decides which decors to include.
