package kernel

// CommandHandler handles a command for a Chamber.
type CommandHandler func(chamer *Chamber, args []string) (int, error)

// DefaultCommandHandler returns success without handling args.
func DefaultCommandHandler(chamer *Chamber, args []string) (int, error) {
	return Success, nil
}

// ErrorHandler maps a command error to an exit code.
type ErrorHandler func(
	chamer *Chamber,
	args []string,
	exitCode int,
	err error,
) int

// DefaultErrorHandler returns GeneralError for any command error.
func DefaultErrorHandler(
	chamer *Chamber,
	args []string,
	exitCode int,
	err error,
) int {
	return GeneralError
}
