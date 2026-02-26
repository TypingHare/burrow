package kernel

import (
	"fmt"

	"github.com/spf13/cobra"
)

// ChamberError is an error that occurs when a Chamber operation fails.
type ChamberError struct {
	// ChamberName is the name of the Chamber that the error occurred in.
	ChamberName string

	// Op is the operation that was being performed when the error occurred.
	Op string

	// Err is the underlying error that caused the ChamberError.
	Err error
}

// NewChamberError creates a new ChamberError with the given chamber name,
// operation, and underlying error.
func NewChamberError(chamberName, op string, err error) *ChamberError {
	return &ChamberError{
		ChamberName: chamberName,
		Op:          op,
		Err:         err,
	}
}

// Error returns a string representation of the ChamberError.
func (e *ChamberError) Error() string {
	return e.ChamberName + " " + e.Op + ": " + e.Err.Error()
}

// Unwrap returns the underlying error that caused the ChamberError.
func (e *ChamberError) Unwrap() error { return e.Err }

// Handler is a function that executes a command in a Chamber. It takes the
// Chamber and the command arguments as input and returns an exit code and an
// error if the command fails.
type Handler func(chamber *Chamber, args []string) (int, error)

// Chamber is an independent CLI application inside the Burrow.
type Chamber struct {
	// burrow is the Burrow that this Chamber belongs to.
	burrow *Burrow

	// name is the name of the Chamber.
	name string

	// blueprint is the Blueprint that defines the configuration of the Chamber.
	blueprint Blueprint

	// handler is the Handler that will be used to execute the Chamber's
	// commands.
	Handler Handler

	// RootCommand is the root command.
	RootCommand *cobra.Command

	// Renovator is responsible for managing the decorations of this Chamber.
	renovator *Renovator
}

// NewChamber creates a new Chamber for the given Burrow.
func NewChamber(burrow *Burrow, name string, blueprint Blueprint) *Chamber {
	chamber := &Chamber{
		burrow:      burrow,
		name:        name,
		blueprint:   blueprint,
		Handler:     DefaultHandler,
		RootCommand: &cobra.Command{Use: name},
	}

	chamber.renovator = NewRenovator(chamber)

	return chamber
}

// Burrow returns the Burrow that this Chamber belongs to.
func (c *Chamber) Burrow() *Burrow {
	return c.burrow
}

// Name returns the name of the Chamber.
func (c *Chamber) Name() string {
	return c.name
}

// Blueprint returns the Blueprint of the Chamber.
func (c *Chamber) Blueprint() Blueprint {
	return c.blueprint
}

// Renovator returns the Renovator of the Chamber.
func (c *Chamber) Renovator() *Renovator {
	return c.renovator
}

// Init initializes the Chamber by setting up its environment and dependencies.
func (c *Chamber) Init() error {
	decorationIDs, err := c.blueprint.GetDependencies()
	if err != nil {
		return fmt.Errorf(
			"failed to get dependencies for chamber '%s': %w",
			c.name,
			err,
		)
	}

	// TODO: Implement dependency graph
	for _, decorationID := range decorationIDs {
		rawSpec, err := c.blueprint.GetRawSpec(decorationID)
		if err != nil {
			return fmt.Errorf(
				"failed to get raw spec for dependency '%s': %w",
				decorationID,
				err,
			)
		}

		decoration, err := c.renovator.AddDecoration(decorationID, rawSpec)
		if err != nil {
			return fmt.Errorf(
				"failed to add dependency '%s': %w",
				decorationID,
				err,
			)
		}

		decoration.Assemble()
	}

	return nil
}

// AddCommand adds a command to the chamber's root command.
func (c *Chamber) AddCommand(commandFactory func(*Chamber) *cobra.Command) {
	c.RootCommand.AddCommand(commandFactory(c))
}

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
