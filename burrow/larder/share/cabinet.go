package share

import (
	"bytes"
	"encoding/csv"
	"errors"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
)

type Cabinet[T any] struct {
	path         string
	serializer   func(T) []string
	deserializer func([]string) (T, error)
	Objects      []T
}

func NewCabinet[T any](
	path string,
	serializer func(T) []string,
	deserializer func([]string) (T, error),
) *Cabinet[T] {
	return &Cabinet[T]{
		path:         path,
		serializer:   serializer,
		deserializer: deserializer,
		Objects:      make([]T, 0),
	}
}

// Path returns the file path where the cabinet's data is stored.
func (c *Cabinet[T]) Path() string {
	return c.path
}

func (c *Cabinet[T]) Deserialize(str string) (T, error) {
	reader := csv.NewReader(strings.NewReader(str))
	items, err := reader.Read()
	if err != nil && err != io.EOF {
		var zero T
		return zero, err
	}

	return c.deserializer(items)
}

func (c *Cabinet[T]) Serialize(object T) string {
	items := c.serializer(object)
	var buf bytes.Buffer
	writer := csv.NewWriter(&buf)

	_ = writer.Write(items)
	writer.Flush()

	return strings.TrimSuffix(buf.String(), "\n")
}

func (c *Cabinet[T]) Clear() {
	c.Objects = c.Objects[:0]
}

func (c *Cabinet[T]) Load() error {
	data, err := os.ReadFile(c.path)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			return nil
		}
		return fmt.Errorf("failed to read file %q: %w", c.path, err)
	}

	tempObjects := make([]T, len(c.Objects))
	copy(tempObjects, c.Objects)

	rollback := func() {
		c.Objects = tempObjects
	}

	c.Clear()

	reader := csv.NewReader(strings.NewReader(string(data)))
	for {
		record, err := reader.Read()
		if err == io.EOF {
			break
		}
		if err != nil {
			rollback()
			return fmt.Errorf("failed to read CSV from %q: %w", c.path, err)
		}

		if len(record) == 0 {
			continue
		}

		object, err := c.deserializer(record)
		if err != nil {
			rollback()
			return fmt.Errorf(
				"failed to deserialize record %q: %w",
				record,
				err,
			)
		}
		c.Objects = append(c.Objects, object)
	}

	return nil
}

func (c *Cabinet[T]) Save() error {
	lines := make([]string, 0, len(c.Objects))
	for _, object := range c.Objects {
		lines = append(lines, c.Serialize(object))
	}

	dir := filepath.Dir(c.path)
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return fmt.Errorf("failed to create directory %s: %w", dir, err)
	}

	return os.WriteFile(c.path, []byte(strings.Join(lines, "\n")), 0o644)
}

func GetCabinet[T any](
	d LarderDecorationLike,
	name string,
) (*Cabinet[T], error) {
	cabinet, exists := d.CabinetMap()[name]
	if !exists || cabinet == nil {
		return nil, fmt.Errorf("cabinet with name %s does not exist", name)
	}

	typedCabinet, ok := cabinet.(*Cabinet[T])
	if !ok {
		return typedCabinet, fmt.Errorf(
			"cabinet with name %s is not of expected type, got %T",
			name,
			cabinet,
		)
	}

	return typedCabinet, nil
}

func AddCabinet[T any](
	d LarderDecorationLike,
	name string,
	serializer func(T) []string,
	deserializer func([]string) (T, error),
) (*Cabinet[T], error) {
	if _, exists := d.CabinetMap()[name]; exists {
		return nil, fmt.Errorf("cabinet with name %s already exists", name)
	}

	cabinetFilePath := GetCabinetFilePath(d.Chamber(), name)
	cabinet := NewCabinet(cabinetFilePath, serializer, deserializer)
	d.CabinetMap()[name] = cabinet

	return cabinet, nil
}
