package shell

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/shell/command"
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

type ShellDecoration struct {
	kernel.Decoration[share.ShellSpec]
}

func (d *ShellDecoration) Dependencies() []string {
	return []string{
		"core@github.com/TypingHare/burrow",
	}
}

func (d *ShellDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"shebang":          d.Spec().Shebang,
		"fileName":         d.Spec().FileName,
		"createdFileNames": d.Spec().CreatedFileNames,
	}
}

func (d *ShellDecoration) Assemble() error {
	coreDecoration, err := kernel.Use[*core.CoreDecoration](d.Chamber())
	if err != nil {
		return fmt.Errorf("failed to use core decoration: %w", err)
	}

	coreDecoration.AddCommand(command.ShellCommand(d.Chamber(), d))

	return nil
}

func (d *ShellDecoration) Launch() error      { return nil }
func (d *ShellDecoration) Terminate() error   { return nil }
func (d *ShellDecoration) Disassemble() error { return nil }

func BuildCoreDecoration(
	chamber *kernel.Chamber,
	spec share.ShellSpec,
) (kernel.DecorationInstance, error) {
	decoration := &ShellDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}

	return decoration, nil
}
