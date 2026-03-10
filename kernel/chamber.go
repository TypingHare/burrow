package kernel

import (
	"path/filepath"
)

// Chamber represents an independent CLI application within a Burrow.
type Chamber struct {
	Burrow    *Burrow
	Name      string
	Blueprint Blueprint
	Renovator *Renovator
	Handler   Handler
}

// NewChamber returns a new Chamber for burrow, name, and blueprint.
func NewChamber(burrow *Burrow, name string, blueprint Blueprint) *Chamber {
	chamber := &Chamber{
		Burrow:    burrow,
		Name:      name,
		Blueprint: blueprint,
		Handler:   DefaultHandler,
	}

	chamber.Renovator = NewRenovator(chamber)

	return chamber
}

// IsRoot reports whether c is the Burrow's root chamber.
func (c *Chamber) IsRoot() bool {
	return c.Name == c.Burrow.Env.Get(EnvRootChamber)
}

// Error returns a ChamberError for c.
func (c *Chamber) Error(msg string, err error) *ChamberError {
	return NewChamberError(c.Name, msg, err)
}

// UpdateBlueprint updates the chamber blueprint with the current state of its
// decors.
func (c *Chamber) UpdateBlueprint() error {
	decorsByIDs := c.Renovator.decorsByIDs
	for _, decorID := range c.Blueprint.GetDecorIDs() {
		if decor, exists := decorsByIDs[decorID]; exists {
			c.Blueprint[decorID] = decor.RawSpec()
		}
	}

	return nil
}

// UpdateAndSaveBlueprint updates the chamber blueprint with the current state
// of its decors and saves it through the Burrow's Architect.
func (c *Chamber) UpdateAndSaveBlueprint() error {
	if err := c.UpdateBlueprint(); err != nil {
		return c.Error("failed to update blueprint before saving", err)
	}

	return c.Burrow.architect.SaveBlueprint(c.Name, c.Blueprint)
}

// GetConfigDir returns the path to the chamber's config directory, which is a
// subdirectory of the Burrow's config directory named after the chamber.
func (c *Chamber) GetConfigDir() string {
	return filepath.Join(c.Burrow.GetConfigDir(), c.Name)
}

// GetDataDir returns the path to the chamber's data directory, which is a
// subdirectory of the Burrow's data directory named after the chamber.
func (c *Chamber) GetDataDir() string {
	return filepath.Join(c.Burrow.GetChamberDir(), c.Name)
}

// init resolves the chamber's root decoration dependencies before command
// execution.
// func (c *Chamber) init() error {
// 	decorationIDs := c.Blueprint.GetDecorIDs()
// 	if err := c.Renovator.resolveRootDependencies(decorationIDs); err != nil {
// 		return err
// 	}
//
// 	return c.installDecorations()
// }
//
// // installDecorations assembles and launches decorations in dependency order.
// func (c *Chamber) installDecorations() error {
// 	assembledDecorations := []DecorationInstance{}
// 	for i, decoration := range c.Renovator.orderedDecorations {
// 		if err := decoration.Assemble(); err != nil {
// 			// Clean up previously assembled decorations in reverse order.
// 			for j := len(assembledDecorations) - 1; j >= 0; j-- {
// 				err := assembledDecorations[j].Disassemble()
// 				if err != nil {
// 					fmt.Fprintf(
// 						os.Stderr,
// 						"failed to disassemble decoration %q during "+
// 							"cleanup: %v\n",
// 						c.Renovator.orderedDecorationIDs[j],
// 						err,
// 					)
// 				}
// 			}
//
// 			return c.Error(
// 				fmt.Sprintf(
// 					"failed to assemble decoration %q",
// 					c.Renovator.orderedDecorationIDs[i],
// 				),
// 				err,
// 			)
// 		}
//
// 		assembledDecorations = append(assembledDecorations, decoration)
// 	}
//
// 	launchedDecorations := []DecorationInstance{}
// 	for i, decoration := range c.Renovator.orderedDecorations {
// 		if err := decoration.Launch(); err != nil {
// 			// Clean up previously launched decorations in reverse order.
// 			for j := len(launchedDecorations) - 1; j >= 0; j-- {
// 				err := launchedDecorations[j].Terminate()
// 				if err != nil {
// 					fmt.Fprintf(
// 						os.Stderr,
// 						"failed to terminate decoration %q during "+
// 							"cleanup: %v\n",
// 						c.Renovator.orderedDecorationIDs[j],
// 						err,
// 					)
// 				}
// 			}
//
// 			for j := len(assembledDecorations) - 1; j >= 0; j-- {
// 				err := assembledDecorations[j].Disassemble()
// 				if err != nil {
// 					fmt.Fprintf(
// 						os.Stderr,
// 						"failed to disassemble decoration %q during "+
// 							"cleanup: %v\n",
// 						c.Renovator.orderedDecorationIDs[j],
// 						err,
// 					)
// 				}
// 			}
//
// 			return c.Error(
// 				fmt.Sprintf(
// 					"failed to launch decoration %q",
// 					c.Renovator.orderedDecorationIDs[i],
// 				),
// 				err,
// 			)
// 		}
//
// 		launchedDecorations = append(launchedDecorations, decoration)
// 	}
//
// 	return nil
// }
//
// // discardDecorations terminates and disassembles the chamber's decorations in
// // reverse dependency order.
// func (c *Chamber) discardDecorations() error {
// 	for i := len(c.Renovator.orderedDecorations) - 1; i >= 0; i-- {
// 		decoration := c.Renovator.orderedDecorations[i]
// 		if err := decoration.Terminate(); err != nil {
// 			return c.Error(
// 				fmt.Sprintf(
// 					"failed to terminate decoration %q",
// 					c.Renovator.orderedDecorationIDs[i],
// 				),
// 				err,
// 			)
// 		}
// 	}
//
// 	for i := len(c.Renovator.orderedDecorations) - 1; i >= 0; i-- {
// 		decoration := c.Renovator.orderedDecorations[i]
// 		if err := decoration.Disassemble(); err != nil {
// 			return c.Error(
// 				fmt.Sprintf(
// 					"failed to disassemble decoration %q",
// 					c.Renovator.orderedDecorationIDs[i],
// 				),
// 				err,
// 			)
// 		}
// 	}
//
// 	return nil
// }
