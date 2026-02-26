package kernel

// Vars is a simple string key-value store.
type Vars map[string]string

// NewVars creates a new Vars instance.
func NewVars() Vars {
	return make(Vars)
}

// Set sets the value of the environment variable with the given key. If the key
// is empty, it does nothing. If the value is empty, it deletes the key from the
// Vars.
func (v Vars) Set(key string, value string) {
	if key == "" {
		return
	}

	if value == "" {
		delete(v, key)
	}

	v[key] = value
}

// Get returns the value of the environment variable with the given key. If the
// key does not exist, it returns an empty string.
func (v Vars) Get(key string) string {
	return v[key]
}
