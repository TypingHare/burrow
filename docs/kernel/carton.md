### Carton

A **carton** is a container that holds a set of [decors](./decor.md)

```go
type Carton struct {
	Metadata Vars
	DecorDefsByNames map[string]DecorDef
}
```
