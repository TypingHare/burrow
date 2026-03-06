package kernel

import (
	"fmt"
	"os"
	"path/filepath"
	"reflect"
)

// Chamber represents an independent CLI application within a Burrow.
type Chamber struct {
	burrow *Burrow

	name string

	blueprint Blueprint

	renovator *Renovator

	// Handler executes commands for the chamber.
	Handler Handler
}

// NewChamber returns a new Chamber for burrow, name, and blueprint.
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

// Burrow returns the Burrow that contains c.
func (c *Chamber) Burrow() *Burrow {
	return c.burrow
}

// Name returns the chamber name.
func (c *Chamber) Name() string {
	return c.name
}

// Blueprint returns the chamber blueprint.
func (c *Chamber) Blueprint() Blueprint {
	return c.blueprint
}

// Renovator returns the chamber renovator.
func (c *Chamber) Renovator() *Renovator {
	return c.renovator
}

// IsRoot reports whether c is the Burrow's root chamber.
func (c *Chamber) IsRoot() bool {
	return c.name == c.burrow.Env.Get(EnvRootChamber)
}

// Error returns a ChamberError for c.
func (c *Chamber) Error(msg string, err error) *ChamberError {
	return NewChamberError(c.name, msg, err)
}

// UpdateBlueprint updates the chamber blueprint with the current state of its
// decorations.
func (c *Chamber) UpdateBlueprint() error {
	decorationsByID := c.renovator.decorationsByID
	for decorationID := range c.blueprint {
		if decoration, exists := decorationsByID[decorationID]; exists {
			c.blueprint[decorationID] = decoration.RawSpec()
		}
	}

	return nil
}

// UpdateAndSaveBlueprint updates the chamber blueprint with the current state
// of its decorations and saves it through the Burrow's Architect.
func (c *Chamber) UpdateAndSaveBlueprint() error {
	if err := c.UpdateBlueprint(); err != nil {
		return c.Error("failed to update blueprint before saving", err)
	}

	return c.burrow.architect.SaveBlueprint(c.name, c.blueprint)
}

// GetDataDir returns the path to the chamber's data directory, which is a
// subdirectory of the Burrow's data directory named after the chamber.
func (c *Chamber) GetDataDir() string {
	return filepath.Join(c.Burrow().GetChamberDir(), c.Name())
}

// Use returns the decoration of type T installed in chamber.
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

// init resolves the chamber's root decoration dependencies before command
// execution.
func (c *Chamber) init() error {
	decorationIDs := c.blueprint.GetDecorationIDs()
	if err := c.renovator.resolveRootDependencies(decorationIDs); err != nil {
		return err
	}

	return c.installDecorations()
}

// installDecorations assembles and launches decorations in dependency order.
func (c *Chamber) installDecorations() error {
	assembledDecorations := []DecorationInstance{}
	for i, decoration := range c.renovator.orderedDecorations {
		if err := decoration.Assemble(); err != nil {
			// Clean up previously assembled decorations in reverse order.
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
			// Clean up previously launched decorations in reverse order.
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

// discardDecorations terminates and disassembles the chamber's decorations in
// reverse dependency order.
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
