package larder

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

// Cabinet stores records of type T in a file.
type Cabinet[T any] struct {
	path         string
	serializer   func(T) []string
	deserializer func([]string) (T, error)
	objects      []T
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
		objects:      make([]T, 0),
	}
}

// Deserialize converts a CSV string into an object of type T using the
// deserializer function.
func (c *Cabinet[T]) Deserialize(str string) (T, error) {
	reader := csv.NewReader(strings.NewReader(str))
	items, err := reader.Read()
	if err != nil && err != io.EOF {
		var zero T
		return zero, err
	}

	return c.deserializer(items)
}

// Serialize converts an object of type T into a CSV string using the serializer
// function.
func (c *Cabinet[T]) Serialize(object T) string {
	items := c.serializer(object)
	var buf bytes.Buffer
	writer := csv.NewWriter(&buf)

	_ = writer.Write(items)
	writer.Flush()

	return strings.TrimSuffix(buf.String(), "\n")
}

// Clear removes all objects from the cabinet's objects slice.
func (c *Cabinet[T]) Clear() {
	c.objects = c.objects[:0]
}

// Load reads the file at the cabinet's path, deserializes each CSV record into
// an object of type T, and stores them in the cabinet's objects slice. If the
// file does not exist, it does nothing.
func (c *Cabinet[T]) Load() error {
	data, err := os.ReadFile(c.path)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			return nil
		}
		return fmt.Errorf("failed to read file %q: %w", c.path, err)
	}

	tempObjects := make([]T, len(c.objects))
	copy(tempObjects, c.objects)

	c.Clear()

	reader := csv.NewReader(strings.NewReader(string(data)))
	for {
		record, err := reader.Read()
		if err == io.EOF {
			break
		}
		if err != nil {
			c.objects = tempObjects
			return fmt.Errorf("failed to read CSV from %q: %w", c.path, err)
		}

		if len(record) == 0 {
			continue
		}

		object, err := c.deserializer(record)
		if err != nil {
			c.objects = tempObjects
			return fmt.Errorf(
				"failed to deserialize record %q: %w",
				record,
				err,
			)
		}
		c.objects = append(c.objects, object)
	}

	return nil
}

// Save serializes each object in the cabinet's objects slice and writes them to
// the file at the cabinet's path, with each object on a new line.
func (c *Cabinet[T]) Save() error {
	lines := make([]string, 0, len(c.objects))
	for _, object := range c.objects {
		lines = append(lines, c.Serialize(object))
	}

	dir := filepath.Dir(c.path)
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return fmt.Errorf("failed to create directory %s: %w", dir, err)
	}

	return os.WriteFile(c.path, []byte(strings.Join(lines, "\n")), 0o644)
}
