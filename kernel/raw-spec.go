package kernel

import "fmt"

// RawSpec is an unstructured representation of a decoration specification.
type RawSpec map[string]any

// NewRawSpec returns an empty RawSpec.
func NewRawSpec() RawSpec {
	return make(RawSpec)
}

// GetRawSpecValue returns the value stored at key as type T.
//
// It reports whether key is present. If the key exists but the value cannot be
// asserted to T, GetRawSpecValue returns an error.
func GetRawSpecValue[T any](rawSpec RawSpec, key string) (T, bool, error) {
	value, exists := rawSpec[key]
	if !exists {
		var zeroValue T
		return zeroValue, false, nil
	}

	typedValue, ok := value.(T)
	if !ok {
		var zeroValue T
		return zeroValue, true, fmt.Errorf(
			"raw spec value for key %q has type %T, not %T",
			key,
			value,
			zeroValue,
		)
	}

	return typedValue, true, nil
}

// GetRawSpecValueOrDefault returns the value stored at key as type T, or
// defaultValue if key is not present.
//
// If the key exists but the value cannot be asserted to T,
// GetRawSpecValueOrDefault returns an error.
func GetRawSpecValueOrDefault[T any](
	rawSpec RawSpec,
	key string,
	defaultValue T,
) (T, error) {
	value, exists, err := GetRawSpecValue[T](rawSpec, key)
	if err != nil {
		return defaultValue, err
	}
	if !exists {
		return defaultValue, nil
	}

	return value, nil
}
