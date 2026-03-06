package kernel

// Vars is a minimalistic string key-value store.
type Vars map[string]string

// NewVars creates a new Vars instance.
func NewVars() Vars {
	return make(Vars)
}

// Set stores value for key.
//
// If key is empty, Set is a no-op. If value is empty, Set deletes key.
func (v Vars) Set(key string, value string) {
	if key == "" {
		return
	}

	if value == "" {
		delete(v, key)
	} else {
		v[key] = value
	}
}

// Get returns the value for key, or an empty string when key is not present.
func (v Vars) Get(key string) string {
	return v[key]
}
