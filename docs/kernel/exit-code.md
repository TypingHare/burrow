### Exit Code

An exit code is an integer value returned by a process to indicate its termination status. Conventionally, an exit code of `0` indicates that the process completed successfully, while a non-zero exit code indicates that an error occurred. Common exit codes are defined in `kernel/exit_code.go`. In particular, in Burrow:

- `1` indicates a general error caused by the process.
- `2` indicates that the process was used incorrectly, such as with invalid command-line arguments.
- `126` indicates that the command cannot be executed, often due to insufficient permissions.
- `127` indicates that the command was not found.
