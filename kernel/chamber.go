package kernel

import (
	"fmt"
	"os"
	"reflect"
)

// Chamber is an independent CLI application within a Burrow.
type Chamber struct {
	// burrow is the Burrow that contains the Chamber.
	burrow *Burrow

	// name is the Chamber name.
	name string

	// blueprint defines the Chamber configuration.
	blueprint Blueprint

	// renovator manages the Chamber's decorations.
	renovator *Renovator

	// Handler executes commands for the Chamber.
	Handler Handler
}

// NewChamber returns a Chamber for burrow, name, and blueprint.
func NewChamber(burrow *Burrow, name string, blueprint Blueprint) *Chamber {
	chamber := &Chamber{
		burrow:    burrow,
		name:      name,
		blueprint: blueprint,
		Handler:   DefaultHandler,
	}

	chamber.renovator = NewRenovator(chamber)

	return chamber
}

// Burrow returns the Burrow that contains the Chamber.
func (c *Chamber) Burrow() *Burrow {
	return c.burrow
}

// Name returns the Chamber name.
func (c *Chamber) Name() string {
	return c.name
}

// Blueprint returns the Chamber's Blueprint.
func (c *Chamber) Blueprint() Blueprint {
	return c.blueprint
}

// Renovator returns the Chamber's Renovator.
func (c *Chamber) Renovator() *Renovator {
	return c.renovator
}

// IsRoot reports whether the Chamber is the Burrow's root Chamber.
func (c *Chamber) IsRoot() bool {
	return c.name == c.burrow.Env.Get(EnvRootChamber)
}

// Error returns a ChamberError for the Chamber.
func (c *Chamber) Error(msg string, err error) *ChamberError {
	return NewChamberError(c.name, msg, err)
}

// Use retrieves a decoration of the specified type from the Chamber's
// Renovator.
func Use[T DecorationInstance](chamber *Chamber) (T, error) {
	var zero T

	decorationType := reflect.TypeFor[T]()
	decoration, ok := chamber.Renovator().GetDecorationByType(decorationType)
	if !ok {
		return zero, chamber.Error(
			fmt.Sprintf("decoration %v is not installed", decorationType),
			nil,
		)
	}

	typed, ok := decoration.(T)
	if !ok {
		return zero, chamber.Error(
			fmt.Sprintf(
				"cast decoration expected %v but got %T",
				decorationType,
				decoration,
			),
			nil,
		)
	}

	return typed, nil
}

// init resolves the Chamber's root decoration dependencies.
//
// init should be called before executing commands so required dependencies are
// available.
func (c *Chamber) init() error {
	decorationIDs := c.blueprint.GetDecorationIDs()
	if err := c.renovator.resolveRootDependencies(decorationIDs); err != nil {
		return c.Error("failed to resolve root dependencies", err)
	}

	return c.installDecorations()
}

func (c *Chamber) installDecorations() error {
	assembledDecorations := []DecorationInstance{}
	for i, decoration := range c.renovator.orderedDecorations {
		if err := decoration.Assemble(); err != nil {
			// Disassemble any previously assembled decorations in reverse order
			// to clean up.
			for j := len(assembledDecorations) - 1; j >= 0; j-- {
				err := assembledDecorations[j].Disassemble()
				if err != nil {
					fmt.Fprintf(
						os.Stderr,
						"failed to disassemble decoration %q during "+
							"cleanup: %v\n",
						c.renovator.orderedDecorationIDs[j],
						err,
					)
				}
			}

			return c.Error(
				fmt.Sprintf(
					"failed to assemble decoration %q",
					c.renovator.orderedDecorationIDs[i],
				),
				err,
			)
		}

		assembledDecorations = append(assembledDecorations, decoration)
	}

	launchedDecorations := []DecorationInstance{}
	for i, decoration := range c.renovator.orderedDecorations {
		if err := decoration.Launch(); err != nil {
			// Terminate any previously launched decorations in reverse order to
			// clean up.
			for j := len(launchedDecorations) - 1; j >= 0; j-- {
				err := launchedDecorations[j].Terminate()
				if err != nil {
					fmt.Fprintf(
						os.Stderr,
						"failed to terminate decoration %q during "+
							"cleanup: %v\n",
						c.renovator.orderedDecorationIDs[j],
						err,
					)
				}
			}

			for j := len(assembledDecorations) - 1; j >= 0; j-- {
				err := assembledDecorations[j].Disassemble()
				if err != nil {
					fmt.Fprintf(
						os.Stderr,
						"failed to disassemble decoration %q during "+
							"cleanup: %v\n",
						c.renovator.orderedDecorationIDs[j],
						err,
					)
				}
			}

			return c.Error(
				fmt.Sprintf(
					"failed to launch decoration %q",
					c.renovator.orderedDecorationIDs[i],
				),
				err,
			)
		}

		launchedDecorations = append(launchedDecorations, decoration)
	}

	return nil
}

// discardDecorations terminates and disassembles the Chamber's decorations in
// reverse dependency order.
//
// discardDecorations should be called before burying the Chamber to ensure
// decorations are properly cleaned up.
func (c *Chamber) discardDecorations() error {
	for i := len(c.renovator.orderedDecorations) - 1; i >= 0; i-- {
		decoration := c.renovator.orderedDecorations[i]
		if err := decoration.Terminate(); err != nil {
			return c.Error(
				fmt.Sprintf(
					"failed to terminate decoration %q",
					c.renovator.orderedDecorationIDs[i],
				),
				err,
			)
		}
	}

	for i := len(c.renovator.orderedDecorations) - 1; i >= 0; i-- {
		decoration := c.renovator.orderedDecorations[i]
		if err := decoration.Disassemble(); err != nil {
			return c.Error(
				fmt.Sprintf(
					"failed to disassemble decoration %q",
					c.renovator.orderedDecorationIDs[i],
				),
				err,
			)
		}
	}

	return nil
}
