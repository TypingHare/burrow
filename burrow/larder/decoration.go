package larder

import (
	"github.com/TypingHare/burrow/v2026/burrow/larder/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// LarderDecoration provides typed cabinets backed by files under the chamber's
// data directory.
type LarderDecoration struct {
	// Decoration carries the typed larder spec and chamber reference.
	kernel.Decoration[share.LarderSpec]

	// cabinetMap stores named cabinets registered by other decorations.
	cabinetMap map[string]any
}

// Dependencies declares required decorations for larder features.
func (d *LarderDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

// RawSpec serializes the larder spec into the blueprint raw-spec format.
func (d *LarderDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

// CabinetMap returns the mutable registry of named cabinets for this chamber.
func (d *LarderDecoration) CabinetMap() map[string]any {
	return d.cabinetMap
}

// Assemble prepares runtime state for larder. Larder has no assembly step.
func (d *LarderDecoration) Assemble() error { return nil }

// Launch starts runtime behavior after assembly. Larder has no launch step.
func (d *LarderDecoration) Launch() error { return nil }

// Terminate stops runtime behavior before disassembly. Larder has no terminate
// step.
func (d *LarderDecoration) Terminate() error { return nil }

// Disassemble releases resources created during assembly. Larder has no
// disassembly step.
func (d *LarderDecoration) Disassemble() error { return nil }

// BuildLarderDecoration constructs a larder decoration for the given chamber
// and parsed larder spec.
func BuildLarderDecoration(
	chamber *kernel.Chamber,
	spec *share.LarderSpec,
) (kernel.DecorationInstance, error) {
	return &LarderDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
		cabinetMap: make(map[string]any),
	}, nil
}

// UseDecoration resolves the larder decoration from the same chamber as d.
func UseDecoration(
	d kernel.DecorationInstance,
) (*LarderDecoration, error) {
	return kernel.Use[*LarderDecoration](d.Chamber())
}
