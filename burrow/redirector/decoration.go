package redirector

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/redirector/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// RedirectorDecoration retries failed command execution by rewriting args
// through a redirector function and then re-running the core command tree.
type RedirectorDecoration struct {
	// Decoration carries the typed redirector spec and chamber reference.
	kernel.Decoration[share.RedirectorSpec]

	// Redirector rewrites command arguments after an initial execution failure.
	Redirector share.Redirector
}

// Dependencies declares required decorations for redirector features.
func (d *RedirectorDecoration) Dependencies() []string {
	return []string{
		kernel.GetDecorationID("core", kernel.CartonName),
	}
}

// RawSpec serializes the redirector spec into the blueprint raw-spec format.
func (d *RedirectorDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"silentlyRedirect": d.Spec().SilentlyRedirect,
	}
}

// GetRedirector returns the redirect function installed on the decoration.
func (d *RedirectorDecoration) GetRedirector() share.Redirector {
	return d.Redirector
}

// Assemble prepares runtime state for redirector. Redirector has no assembly
// step.
func (d *RedirectorDecoration) Assemble() error { return nil }

// Launch installs the redirecting chamber handler when a redirector function
// is present.
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

// Terminate stops runtime behavior before disassembly. Redirector has no
// terminate step.
func (d *RedirectorDecoration) Terminate() error { return nil }

// Disassemble releases resources created during assembly. Redirector has no
// disassembly step.
func (d *RedirectorDecoration) Disassemble() error { return nil }

// BuildRedirectorDecoration constructs a redirector decoration for the given
// chamber and parsed redirector spec.
func BuildRedirectorDecoration(
	chamber *kernel.Chamber,
	spec *share.RedirectorSpec,
) (kernel.DecorationInstance, error) {
	return &RedirectorDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}

// UseDecoration resolves the redirector decoration from the same chamber as d.
func UseDecoration(
	d kernel.DecorationInstance,
) (*RedirectorDecoration, error) {
	return kernel.Use[*RedirectorDecoration](d.Chamber())
}
