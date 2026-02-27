package larder

import (
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

const LarderDirName = "larder"

type LarderSpec struct{}

type LarderDecoration struct {
	kernel.Decoration[LarderSpec]
	cabinetMap map[string]any
}

func (d *LarderDecoration) SpecAny() any           { return d.Spec() }
func (d *LarderDecoration) Dependencies() []string { return []string{} }

// SetCabinet adds a cabinet to the larder decoration.
func SetCabinet[T any](
	d *LarderDecoration,
	name string,
	cabinet *Cabinet[T],
) error {
	if _, exists := d.cabinetMap[name]; exists {
		return d.Chamber().Error("cabinet already exists: "+name, nil)
	}

	d.cabinetMap[name] = cabinet

	return nil
}

// GetCabinet retrieves a cabinet from the larder decoration by name.
func GetCabinet[T any](d *LarderDecoration, name string) (*Cabinet[T], error) {
	value, exists := d.cabinetMap[name]
	if !exists {
		return nil, d.Chamber().Error("cabinet does not exist: "+name, nil)
	}
	cabinet, exists := value.(*Cabinet[T])

	return cabinet, nil
}

// CreateCabinet creates a new cabinet and adds it to the larder decoration.
func CreateCabinet[T any](
	d *LarderDecoration,
	name string,
	serializer func(T) []string,
	deserializer func([]string) (T, error),
) error {
	path := filepath.Join(d.Chamber().GetDataDir(), LarderDirName, name)
	cabinet := NewCabinet(path, serializer, deserializer)
	return SetCabinet(d, name, cabinet)
}

func (d *LarderDecoration) Assemble() error {
	return nil
}

func (d *LarderDecoration) Launch() error      { return nil }
func (d *LarderDecoration) Terminate() error   { return nil }
func (d *LarderDecoration) Disassemble() error { return nil }

func ParseLarderSpec(rawSpec kernel.RawSpec) (LarderSpec, error) {
	return LarderSpec{}, nil
}

func BuildLarderDecoration(
	chamber *kernel.Chamber,
	spec LarderSpec,
) (kernel.DecorationInstance, error) {
	return &LarderDecoration{
		Decoration: *kernel.NewDecoration(chamber, spec),
	}, nil
}
