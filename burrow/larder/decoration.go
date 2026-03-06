package larder

import (
	"github.com/TypingHare/burrow/v2026/burrow/larder/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type LarderDecoration struct {
	kernel.Decoration[share.LarderSpec]
}

func (d *LarderDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *LarderDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

func (d *LarderDecoration) Assemble() error {
	return nil
}

func (d *LarderDecoration) Launch() error      { return nil }
func (d *LarderDecoration) Terminate() error   { return nil }
func (d *LarderDecoration) Disassemble() error { return nil }

func BuildLarderDecoration(
	chamber *kernel.Chamber,
	spec share.LarderSpec,
) (kernel.DecorationInstance, error) {
	return &LarderDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
