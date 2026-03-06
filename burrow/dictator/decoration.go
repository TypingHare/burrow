package dictator

import (
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type DictatorDecoration struct {
	kernel.Decoration[share.DictatorSpec]
}

func (d *DictatorDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *DictatorDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

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

func (d *DictatorDecoration) Launch() error      { return nil }
func (d *DictatorDecoration) Terminate() error   { return nil }
func (d *DictatorDecoration) Disassemble() error { return nil }

func BuildDictatorDecoration(
	chamber *kernel.Chamber,
	spec share.DictatorSpec,
) (kernel.DecorationInstance, error) {
	return &DictatorDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
