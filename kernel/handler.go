package kernel

import "fmt"

// Handler is a function that executes a command in a Chamber. It takes the
// Chamber and the command arguments as input and returns an exit code and an
// error if the command fails.
type Handler func(chamber *Chamber, args []string) (int, error)

// DefaultHandler is the default Handler for a Chamber. It executes the root
// command of the Chamber with the given arguments.
func DefaultHandler(chamber *Chamber, args []string) (int, error) {
	if chamber == nil {
		return ERROR_NULL_POINTER, fmt.Errorf("chamber is nil")
	}

	rootCommand := chamber.RootCommand
	if rootCommand == nil {
		return ERROR_NULL_POINTER, fmt.Errorf("root command is nil")
	}

	rootCommand.SetArgs(args)
	err := rootCommand.Execute()
	if err != nil {
		return GENERAL_ERROR, err
	}

	return SUCCESS, nil
}
