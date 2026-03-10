### Decor

A **decor** is a component that adds features to a [chamber](./chamber.md). It provides shared structs, interfaces, and functions, and can also modify the behavior of the chamber by updating its blueprints, handlers, and other components. A chamber is composed of a set of decors that work together to provide its functionality.

```go
// kernel/decor.go
type Decor interface {
	Chamber() *Chamber
	RawSpec() RawSpec
	Deps() []string

	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}
```

The `Decor` interface defines the basic structure and behavior of a decor. The `Chamber` method returns the chamber that the decor belongs to. The `RawSpec` method returns the raw specification derived from the current decor specification. The `Deps` method returns the IDs of the decors that this decor depends on.

A decor is created by a [removator](./renovator.md) from a **decor definition**, which will be discussed later. The `Deps` method returns the IDs of the decors it depends on. The renovator resolves these dependencies and produces a topological ordering of the decors.

The `Assemble`, `Launch`, `Terminate`, and `Disassemble` methods define the lifecycle of a decor. When a chamber is being dug, the `Assemble` function of each decor is called according to the topological order. After that, the `Launch` function is then called in the same order, and the chamber becomes ready to serve requests.

When a chamber is being buried, the `Terminate` function of each decor is called in the reverse topological order. Finally, the `Disassemble` function is called in the same reverse order. After this process completes, the chamber is removed from the burrow.

```go
type TypedDecor[S any] struct {
	Chamber *Chamber
	Spec *S
	Deps []string

	OnAssemble    func() error
	OnLaunch      func() error
	OnTerminate   func() error
	OnDisassemble func() error
}
```

The `TypedDecor` struct is an implementation of the `Decor` interface that uses a generic type `S` to represent the [decor specification](./spec.md). The `Deps` field contains the IDs of the decors it depends on. The `OnAssemble`, `OnLaunch`, `OnTerminate`, and `OnDisassemble` fields are function pointers that are called during the corresponding lifecycle stages described above.

Any decor must embed `TypedDecor` to implement the `Decor` interface. For example:

```go
type MyDecor[MySpec] struct {
    TypedDecor[MySpec]
}
```

#### Decor Definition

A **decor definition** is a factory for creating a decor. The `DecorDef` interface defines the methods required to create a decor:

```go
// kernel/decor_def.go
type DecorDef interface {
	Name() string
	Parse(RawSpec) (any, error)
	Build(*Chamber, any) (DecorInstance, error)
}
```

The `Name` method returns the name of the decor definition. The `Parse` method takes a raw specification and parses it into a decor specification. The `Build` method then takes a chamber and the parsed specification to create a decor instance.

```go
// kernal/decor_def.go
type TypedDecorDef[S any] struct {
	DecorName  string
	ParseSpec  func(RawSpec) (*S, error)
	BuildDecor func(*Chamber, *S) (Decor, error)
}
```

The `TypedDecorDef` struct is an implementation of the `DecorDef` interface that uses a generic type `S` to represent the decor specification. The `ParseSpec` field is a function pointer that parses a raw specification into a typed specification. The `BuildDecor` field is a function pointer that builds a decor instance from a chamber and a typed specification.

In every decor package, there must be a `TypedDecorDef` variable that defines the decor definition for carton to register. For example:

```go
var MyDecorDef = TypedDecorDef[MySpec]{
    DecorName: "my",
    ParseSpec: func(raw RawSpec) (*MySpec, error) {
        // Parse raw spec into MySpec
        return nil, nil
    },
    BuildDecor: func(chamber *Chamber, spec *MySpec) (Decor, error) {
        // Build MyDecor from chamber and MySpec
        return nil, nil
     },
}
```
