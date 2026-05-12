package kernel

const (
	// Success indicates that the command completed successfully.
	Success = 0
	// GeneralError indicates an unspecified command failure.
	GeneralError = 1
	// IncorrectUsage indicates invalid command-line usage.
	IncorrectUsage = 2
	// CommandNotExecutable indicates that a command exists but cannot run.
	CommandNotExecutable = 126
	// CommandNotFound indicates that the requested command was not found.
	CommandNotFound = 127
	// ProcessTerminated is the base code for signal-related process exits.
	ProcessTerminated = 128

	// ErrorNullPointer indicates that required runtime state was nil.
	ErrorNullPointer = ProcessTerminated + 11
)
