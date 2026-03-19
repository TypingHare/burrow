### Vars

`Vars` is a string dictionary where values are strings:

```go
// kernel/vars.go
type Vars map[string]string
```

It is used as `Env` in `Burrow` and `Metadata` in `Carton` in the kernel. There are two methods to set and get values in `Vars`. Developers should always use these two functions to manipulate `Vars` instead of directly accessing the map. The `Set` method will delete the key if the value is empty, and the `Get` method will return an empty string if the key does not exist.
