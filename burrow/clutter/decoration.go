package clutter

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/command"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/command/carton"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// ClutterDecoration provides carton-management capabilities, including Burrow
// build flows and carton install/uninstall commands.
type ClutterDecoration struct {
	// Decoration carries the typed clutter spec and chamber reference.
	kernel.Decoration[share.ClutterSpec]
}

// Dependencies declares required decorations for clutter features.
// Clutter extends the command tree exposed by the core decoration.
func (d *ClutterDecoration) Dependencies() []string {
	warehouse := d.Chamber().Burrow().Warehouse()
	return []string{
		warehouse.GetDecorationID("core", kernel.CartonName),
	}
}

// RawSpec serializes the clutter spec into the blueprint raw-spec format.
func (d *ClutterDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"cartonNames":  d.Spec().CartonNames,
		"localCartons": d.Spec().LocalCartons,
		"magicEnv":     d.Spec().MagicEnv,
	}
}

// Assemble mounts clutter commands into the chamber command tree via the core
// decoration.
func (d *ClutterDecoration) Assemble() error {
	if coreDecoration, err := core.UseDecoration(d); err != nil {
		return d.Chamber().Error("failed to use core decoration", err)
	} else {
		if err = coreDecoration.SetCommand(
			nil,
			command.BurrowCommand(d),
		); err != nil {
			return err
		}

		if err = coreDecoration.SetCommand(
			[]string{"carton"},
			carton.InstallCommand(d),
			carton.UninstallCommand(d),
		); err != nil {
			return err
		}
	}

	return nil
}

// Launch starts runtime behavior after assembly. Clutter has no launch step.
func (d *ClutterDecoration) Launch() error { return nil }

// Terminate stops runtime behavior before disassembly. Clutter has no
// terminate step.
func (d *ClutterDecoration) Terminate() error { return nil }

// Disassemble releases resources created during assembly. Clutter has no
// disassembly step.
func (d *ClutterDecoration) Disassemble() error { return nil }

// BuildClutterDecoration constructs a clutter decoration for the given chamber
// and parsed clutter spec.
func BuildClutterDecoration(
	chamber *kernel.Chamber,
	spec share.ClutterSpec,
) (kernel.DecorationInstance, error) {
	return &ClutterDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
