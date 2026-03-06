package dictator

import (
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// DictatorDecoration provides chamber-administration commands (create, destroy,
// dig, bury, list, info) through the core command tree.
type DictatorDecoration struct {
	// Decoration carries the typed dictator spec and chamber reference.
	kernel.Decoration[share.DictatorSpec]
}

// Dependencies declares required decorations for dictator features.
// Dictator mounts its commands through the core decoration.
func (d *DictatorDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

// RawSpec serializes the dictator spec into the blueprint raw-spec format.
// Dictator currently has no persisted configuration fields.
func (d *DictatorDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

// Assemble mounts dictator chamber-management commands into the chamber command
// tree via the core decoration.
func (d *DictatorDecoration) Assemble() error {
	if coreDecoration, err := core.UseDecoration(d); err != nil {
		return err
	} else {
		if err := coreDecoration.SetCommand(
			nil,
			command.ChamberCommand(d),
		); err != nil {
			return err
		}
	}

	return nil
}

// Launch starts runtime behavior after assembly. Dictator has no launch step.
func (d *DictatorDecoration) Launch() error { return nil }

// Terminate stops runtime behavior before disassembly. Dictator has no
// terminate step.
func (d *DictatorDecoration) Terminate() error { return nil }

// Disassemble releases resources created during assembly. Dictator has no
// disassembly step.
func (d *DictatorDecoration) Disassemble() error { return nil }

// BuildDictatorDecoration constructs a dictator decoration for the given
// chamber and parsed dictator spec.
func BuildDictatorDecoration(
	chamber *kernel.Chamber,
	spec share.DictatorSpec,
) (kernel.DecorationInstance, error) {
	return &DictatorDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}

// UseDecoration resolves the dictator decoration from the same chamber as d.
func UseDecoration(
	d kernel.DecorationInstance,
) (*DictatorDecoration, error) {
	return kernel.Use[*DictatorDecoration](d.Chamber())
}
