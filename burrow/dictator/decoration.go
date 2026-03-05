package dictator

import (
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/command/chamber"
	"github.com/TypingHare/burrow/v2026/burrow/dictator/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type DictatorDecoration struct {
	kernel.Decoration[share.DictatorSpec]
}

func (d *DictatorDecoration) Dependencies() []string {
	warehouse := d.Chamber().Burrow().Warehouse()
	return []string{
		warehouse.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *DictatorDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

func (d *DictatorDecoration) Assemble() error {
	if coreDecoration, err := core.UseDecoration(d); err != nil {
		return err
	} else {
		coreDecoration.SetCommand([]string{"chamber"}, chamber.BuryCommand(d))
	}

	return nil
}

func (d *DictatorDecoration) Launch() error      { return nil }
func (d *DictatorDecoration) Terminate() error   { return nil }
func (d *DictatorDecoration) Disassemble() error { return nil }

func (d *DictatorDecoration) BuildDictatorDecoration(
	chamber *kernel.Chamber,
	spec share.DictatorSpec,
) (kernel.DecorationInstance, error) {
	return &DictatorDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
