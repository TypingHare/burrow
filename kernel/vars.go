package kernel

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
