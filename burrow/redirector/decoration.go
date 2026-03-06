package redirector

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/redirector/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type RedirectorDecoration struct {
	kernel.Decoration[share.RedirectorSpec]
	Redirector share.Redirector
}

func (d *RedirectorDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

func (d *RedirectorDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"silentlyRedirect": false,
	}
}

func (d *RedirectorDecoration) GetRedirector() share.Redirector {
	return d.Redirector
}

func (d *RedirectorDecoration) Assemble() error { return nil }
func (d *RedirectorDecoration) Launch() error {
	coreDirection, err := core.UseDecoration(d)
	if err != nil {
		return fmt.Errorf("failed to use core decoration: %w", err)
	}

	if d.Redirector != nil {
		d.Chamber().Handler = share.GetRedirectorHandler(d, coreDirection)
	}

	return nil
}
func (d *RedirectorDecoration) Terminate() error   { return nil }
func (d *RedirectorDecoration) Disassemble() error { return nil }

func BuildRedirectorDecoration(
	chamber *kernel.Chamber,
	spec share.RedirectorSpec,
) (kernel.DecorationInstance, error) {
	return &RedirectorDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}

func UseDecoration(
	d kernel.DecorationInstance,
) (*RedirectorDecoration, error) {
	return kernel.Use[*RedirectorDecoration](d.Chamber())
}
