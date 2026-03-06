package nostalgia

import (
	"github.com/TypingHare/burrow/v2026/burrow/nostalgia/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type NostalgiaDecoration struct {
	kernel.Decoration[share.NostalgiaSpec]
}

func (d *NostalgiaDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *NostalgiaDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

func (d *NostalgiaDecoration) Assemble() error    { return nil }
func (d *NostalgiaDecoration) Launch() error      { return nil }
func (d *NostalgiaDecoration) Terminate() error   { return nil }
func (d *NostalgiaDecoration) Disassemble() error { return nil }

func BuildNostalgiaDecoration(
	chamber *kernel.Chamber,
	spec share.NostalgiaSpec,
) (kernel.DecorationInstance, error) {
	return &NostalgiaDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}

func UseDecoration(
	d kernel.DecorationInstance,
) (*NostalgiaDecoration, error) {
	return kernel.Use[*NostalgiaDecoration](d.Chamber())
}
