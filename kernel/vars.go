package kernel

import (
	"strconv"
	"strings"
)

// Vars is a minimal string key-value store.
type Vars map[string]string

// NewVars returns an initialized Vars map.
func NewVars() Vars {
	return make(Vars)
}

// Set stores value for key.
//
// If key is empty, Set is a no-op.
func (v Vars) Set(key string, value string) {
	if key == "" {
		return
	}

	v[key] = value
}

// Get returns the value for key, or an empty string when key is not present.
func (v Vars) Get(key string) string {
	return v[key]
}

func (v Vars) SetBool(key string, value bool) {
	if value {
		v.Set(key, "1")
	} else {
		v.Set(key, "0")
	}
}

func (v Vars) GetBool(key string) bool {
	return strings.TrimSpace(v.Get(key)) == "1"
}

func (v Vars) SetInt(key string, value int) {
	v.Set(key, strconv.Itoa(value))
}

func (v Vars) GetInt(key string) (int, error) {
	return strconv.Atoi(v.Get(key))
}

func (v Vars) SetFloat(key string, value float64) {
	v.Set(key, strconv.FormatFloat(value, 'f', -1, 64))
}

func (v Vars) GetFloat(key string) (float64, error) {
	return strconv.ParseFloat(v.Get(key), 64)
}
