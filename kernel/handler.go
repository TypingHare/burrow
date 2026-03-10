package kernel

type CommandHandler func(chamer *Chamber, args []string) (int, error)

func DefaultCommandHandler(chamer *Chamber, args []string) (int, error) {
	return Success, nil
}

type ErrorHandler func(
	chamer *Chamber,
	args []string,
	exitCode int,
	err error,
) int

func DefaultErrorHandler(
	chamer *Chamber,
	args []string,
	exitCode int,
	err error,
) int {
	return GeneralError
}
