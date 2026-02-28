package kernel

// Handler executes a Chamber command.
//
// It receives the Chamber and command arguments, and returns an exit code and
// any execution error.
type Handler func(chamber *Chamber, args []string) (int, error)

// DefaultHandler is the default Chamber handler.
func DefaultHandler(chamber *Chamber, args []string) (int, error) {
	return Success, nil
}
