### Handler

In Burrow, a **command handler** is a function that processes a slice of arguments received by a chamber. It is defined as a field in the `Chamber` struct and is responsible for handling an incoming command. A command handler executes the requested operation and returns both an exit code and an error, if one occurs.

```go
// kernerl/handler.go
type CommandHandler func(chamer *Chamber, args []string) (int, error)
```

The returned **exit code** indicates the result of the command execution. By convention, an exit code of `0` represents successful execution, while any non-zero value indicates an error or a specific condition. Common exit codes are defined by the `ExitCode` type. For more details, see the documentation: [ExitCode](./exit-code.md).

The error return value allows the command handler to provide additional information about issues that occur during command processing. If the error is not `nil`, it will be handled by the chamber's error handler:

```go
// kernerl/handler.go
type ErrorHandler func(
	chamer *Chamber,
	args []string,
	exitCode int,
	err error,
) int
```
