package kernel

import (
	"path/filepath"
	"slices"
)

// Chamber represents an independent CLI application within a Burrow.
type Chamber struct {
	// Burrow owns the Chamber.
	Burrow *Burrow

	// Name identifies the Chamber within its Burrow.
	Name string

	// Blueprint describes the decors installed in the Chamber.
	Blueprint Blueprint

	// Renovator resolves and manages the Chamber's decors.
	Renovator *Renovator

	// CommandHandler handles commands routed to the Chamber.
	CommandHandler CommandHandler

	// ErrorHandler maps command errors to process exit codes.
	ErrorHandler ErrorHandler
}

// NewChamber returns a Chamber for burrow, name, and blueprint.
func NewChamber(burrow *Burrow, name string, blueprint Blueprint) *Chamber {
	chamber := &Chamber{
		Burrow:         burrow,
		Name:           name,
		Blueprint:      blueprint,
		CommandHandler: DefaultCommandHandler,
		ErrorHandler:   DefaultErrorHandler,
	}

	chamber.Renovator = NewRenovator(chamber)

	return chamber
}

// Error returns a ChamberError with message and cause.
func (c *Chamber) Error(message string, cause error) *ChamberError {
	return NewChamberError(c.Name, message, cause)
}

// GetConfigDir returns the chamber-specific config directory.
func (c *Chamber) GetConfigDir() string {
	return filepath.Join(c.Burrow.GetConfigDir(), c.Name)
}

// GetDataDir returns the chamber-specific data directory.
func (c *Chamber) GetDataDir() string {
	return filepath.Join(c.Burrow.GetChamberDir(), c.Name)
}

// GetStateDir returns the chamber-specific state directory.
func (c *Chamber) GetStateDir() string {
	return filepath.Join(c.GetStateDir(), c.Name)
}

// InstallDecors installs the decors identified by decorIDs.
func (c *Chamber) InstallDecors(decorIDs []string) error {
	orderedDecors := make([]IDecor, len(decorIDs))
	for i, decorID := range decorIDs {
		decor, exists := c.Renovator.DecorsByIDs[decorID]
		if !exists {
			return c.Error(
				"failed to find decor with ID: "+decorID,
				nil,
			)
		}
		orderedDecors[i] = decor
	}

	// decorIDs already arrives dependency-first, so assemble and launch each
	// dependency before the decors that require it.
	for i := range orderedDecors {
		if err := orderedDecors[i].Assemble(); err != nil {
			return c.Error(
				"failed to assemble decor with ID: "+decorIDs[i],
				err,
			)
		}
	}

	for i := range orderedDecors {
		if err := orderedDecors[i].Launch(); err != nil {
			return c.Error(
				"failed to launch decor with ID: "+decorIDs[i],
				err,
			)
		}
	}

	return nil
}

// UninstallDecors terminates and disassembles the chamber decors.
func (c *Chamber) UninstallDecors(decorIDs []string) error {
	orderedDecors := make([]IDecor, len(decorIDs))
	for i, decorID := range decorIDs {
		decor, exists := c.Renovator.DecorsByIDs[decorID]
		if !exists {
			return c.Error(
				"failed to find decor with ID: "+decorID,
				nil,
			)
		}
		orderedDecors[i] = decor
	}

	// Terminate dependents before their dependencies.
	slices.Reverse(orderedDecors)

	for i := range len(orderedDecors) {
		decor := orderedDecors[i]
		if err := decor.Assemble(); err != nil {
			return c.Error(
				"failed to terminate decor with ID: "+decorIDs[i],
				err,
			)
		}
	}

	for i := len(orderedDecors) - 1; i >= 0; i-- {
		decor := orderedDecors[i]
		if err := decor.Launch(); err != nil {
			return c.Error(
				"failed to disassemble decor with ID: "+decorIDs[i],
				err,
			)
		}
	}

	return nil
}
