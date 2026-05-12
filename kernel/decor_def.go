package kernel

// CreateDecorFunc is a function that creates a Decor for a Chamber and spec.
type CreateDecorFunc func(*Chamber, Vars) (IDecor, error)

// DecorDef describes how to create a Decor.
type DecorDef struct {
	// Name identifies the decor within its carton.
	Name string

	// CreateFunc creates a Decor for a Chamber and spec.
	CreateFunc CreateDecorFunc
}

// NewDecorDef returns a DecorDef with name and buildFunc.
func NewDecorDef(
	name string,
	buildFunc func(*Chamber, Vars) (IDecor, error),
) *DecorDef {
	return &DecorDef{
		Name:       name,
		CreateFunc: buildFunc,
	}
}
