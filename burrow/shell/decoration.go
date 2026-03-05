package shell

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/shell/command"
	"github.com/TypingHare/burrow/v2026/burrow/shell/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

// ShellDecoration provides shell-file management commands and persists shell
// metadata in its spec.
type ShellDecoration struct {
	// Decoration carries the typed shell spec and chamber reference.
	kernel.Decoration[share.ShellSpec]
}

// Dependencies declares required decorations for shell features.
// Shell commands are mounted through the core command tree.
func (d *ShellDecoration) Dependencies() []string {
	return []string{
		"core@github.com/TypingHare/burrow",
	}
}

// RawSpec serializes the shell spec into the blueprint raw-spec format.
func (d *ShellDecoration) RawSpec() kernel.RawSpec {
	return kernel.RawSpec{
		"shebang":          d.Spec().Shebang,
		"fileName":         d.Spec().FileName,
		"createdFileNames": d.Spec().CreatedFileNames,
	}
}

// Assemble mounts the top-level `shell` command into the chamber through the
// core decoration.
func (d *ShellDecoration) Assemble() error {
	coreDecoration, err := kernel.Use[*core.CoreDecoration](d.Chamber())
	if err != nil {
		return fmt.Errorf("failed to use core decoration: %w", err)
	}

	coreDecoration.SetCommand(nil, command.ShellCommand(d))

	return nil
}

// Launch starts runtime behavior after assembly. Shell has no launch step.
func (d *ShellDecoration) Launch() error      { return nil }

// Terminate stops runtime behavior before disassembly. Shell has no terminate
// step.
func (d *ShellDecoration) Terminate() error   { return nil }

// Disassemble releases resources created during assembly. Shell has no
// disassembly step.
func (d *ShellDecoration) Disassemble() error { return nil }

// BuildShellDecoration constructs a shell decoration for the given chamber and
// parsed shell spec.
func BuildShellDecoration(
	chamber *kernel.Chamber,
	spec share.ShellSpec,
) (kernel.DecorationInstance, error) {
	decoration := &ShellDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}

	return decoration, nil
}
