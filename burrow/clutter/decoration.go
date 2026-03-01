package clutter

import (
	"github.com/TypingHare/burrow/v2026/burrow/clutter/command"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/command/carton"
	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
	"github.com/TypingHare/burrow/v2026/burrow/core"
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
		"cartonNames":  d.Spec().CartonNames,
		"localCartons": d.Spec().LocalCartons,
	}
}

func (d *ClutterDecoration) Assemble() error {
	coreDecoration, err := kernel.Use[*core.CoreDecoration](d.Chamber())
	if err != nil {
		return d.Chamber().Error("failed to use core decoration", err)
	}

	coreDecoration.AddCommand(command.BuildCommand(d.Chamber(), d))

	err = coreDecoration.InsertCommand(
		[]string{"carton"},
		carton.InstallCommand(d.Chamber()),
	)
	if err != nil {
		return d.Chamber().Error("failed to insert carton install command", err)
	}

	err = coreDecoration.InsertCommand(
		[]string{"carton"},
		carton.UninstallCommand(d.Chamber()),
	)
	if err != nil {
		return d.Chamber().
			Error("failed to insert carton uninstall command", err)
	}

	return nil
}

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
