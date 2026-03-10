package kernel

type PlanNode struct {
	DecorID      string
	DecorDef     DecorDef
	Dependencies []string
	Dependents   []string
}
