package larder

import (
	"bytes"
	"encoding/csv"
	"errors"
	"io"
	"os"
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

func (c *Cabinet[T]) Load() error {
	data, err := os.ReadFile(c.path)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			return nil
		}
		return err
	}

	c.objects = c.objects[:0]

	lines := strings.SplitSeq(string(data), "\n")
	for line := range lines {
		line = strings.TrimSuffix(line, "\r")
		if strings.TrimSpace(line) == "" {
			continue
		}

		object, err := c.Deserialize(line)
		if err != nil {
			return err
		}
		c.objects = append(c.objects, object)
	}

	return nil
}

func (c *Cabinet[T]) Save() error {
	lines := make([]string, 0, len(c.objects))
	for _, object := range c.objects {
		lines = append(lines, c.Serialize(object))
	}

	return os.WriteFile(c.path, []byte(strings.Join(lines, "\n")), 0o644)
}
