package clutter

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type ClutterDecoration struct {
	kernel.Decoration[share.ClutterSpec]
}

func (d *ClutterDecoration) Dependencies() []string {
	return []string{
		"core@github.com/TypingHare/burrow",
	}
}

func (d *ClutterDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"cartonNames":      d.Spec().CartonNames,
		"localCartonNames": d.Spec().LocalCartonNames,
	}
}

func (d *ClutterDecoration) Assemble() error    { return nil }
func (d *ClutterDecoration) Launch() error      { return nil }
func (d *ClutterDecoration) Terminate() error   { return nil }
func (d *ClutterDecoration) Disassemble() error { return nil }

func BuildClutterDecoration(
	chamber *kernel.Chamber,
	spec share.ClutterSpec,
) (kernel.DecorationInstance, error) {
	return &ClutterDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
