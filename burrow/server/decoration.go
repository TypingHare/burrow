package server

import (
	"github.com/TypingHare/burrow/v2026/burrow/server/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type ServerDecoration struct {
	kernel.Decoration[share.ServerSpec]
}

func (d *ServerDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *ServerDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{}
}

func (d *ServerDecoration) Assemble() error    { return nil }
func (d *ServerDecoration) Launch() error      { return nil }
func (d *ServerDecoration) Terminate() error   { return nil }
func (d *ServerDecoration) Disassemble() error { return nil }

func BuildRedirectorDecoration(
	chamber *kernel.Chamber,
	spec *share.ServerSpec,
) (kernel.DecorationInstance, error) {
	return &ServerDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}

func UseDecoration(
	d kernel.DecorationInstance,
) (*ServerDecoration, error) {
	return kernel.Use[*ServerDecoration](d.Chamber())
}
