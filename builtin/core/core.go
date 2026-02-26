package core

import (
	"github.com/TypingHare/burrow/builtin/core/command"
	"github.com/TypingHare/burrow/internal/kernel"
)

type CoreSpec struct{}

type CoreDecoration struct {
	kernel.Decoration[CoreSpec]
}

func (d *CoreDecoration) SpecAny() any {
	return d.Spec()
}

func (d *CoreDecoration) Assemble() error {
	d.Chamber().AddCommand(command.CartonCommand)
	d.Chamber().AddCommand(command.DecorationCommand)
	return nil
}

func (d *CoreDecoration) Launch() error      { return nil }
func (d *CoreDecoration) Terminate() error   { return nil }
func (d *CoreDecoration) Disassemble() error { return nil }

func ParseCoreSpec(rawSpec kernel.RawSpec) (CoreSpec, error) {
	return CoreSpec{}, nil
}

func BuildCoreDecoration(
	chamber *kernel.Chamber,
	spec CoreSpec,
) (kernel.DecorationInstance, error) {
	return &CoreDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
