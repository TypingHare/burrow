package kernel

// CLI application exit codes for processes.
const (
	Success              = 0
	GeneralError         = 1
	IncorrectUsage       = 2
	CommandNotExecutable = 126
	CommandNotFound      = 127
	ProcessTerminated    = 128

	ErrorNullPointer = ProcessTerminated + 11
)
