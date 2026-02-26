package kernel

// CLI application exit codes for processes.
const (
	SUCCESS                = 0
	GENERAL_ERROR          = 1
	INCORRECT_USAGE        = 2
	COMMAND_NOT_EXECUTABLE = 126
	COMMAND_NOT_FOUND      = 127
	PROCESS_TERMINATED     = 128

	ERROR_NULL_POINTER = PROCESS_TERMINATED + 11
)
