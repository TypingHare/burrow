package kernel

import (
	"errors"
	"fmt"
	"sort"

	"gonum.org/v1/gonum/graph"
	"gonum.org/v1/gonum/graph/simple"
	"gonum.org/v1/gonum/graph/topo"
)

// PlanNode represents a decor in a dependency graph.
type PlanNode struct {
	// GraphNodeID identifies this node in the dependency graph.
	GraphNodeID int64

	// DecorID identifies the decor represented by this node.
	DecorID string

	// DecorDef describes how to create the decor.
	DecorDef *DecorDef

	// Decor is the instantiated decor.
	Decor IDecor

	// Dependencies lists decor IDs that must run before this decor.
	Dependencies []string

	// Dependents lists decor IDs that must run after this decor.
	Dependents []string
}

// Plan tracks a decor dependency graph and its resolved order.
type Plan struct {
	// DependencyGraph connects dependents to the dependencies they require.
	DependencyGraph *simple.DirectedGraph

	// PlanNodesByDecorIDs maps decor IDs to dependency graph nodes.
	PlanNodesByDecorIDs map[string]*PlanNode

	// GraphNodeIDsByDecorIDs maps decor IDs to graph node IDs.
	GraphNodeIDsByDecorIDs map[string]int64

	// DependencyOrder lists decor IDs with dependencies before dependents.
	DependencyOrder []string
}

// NewPlan returns an initialized Plan.
func NewPlan() *Plan {
	return &Plan{
		DependencyGraph:        simple.NewDirectedGraph(),
		PlanNodesByDecorIDs:    make(map[string]*PlanNode),
		GraphNodeIDsByDecorIDs: make(map[string]int64),
		DependencyOrder:        []string{},
	}
}

// CreatePlan resolves decorIDs into a dependency graph and execution order.
func (p *Plan) CreatePlan(renovator *Renovator, decorIDs []string) error {
	err := p.ResolveDependencies(renovator, decorIDs, nil)
	if err != nil {
		return renovator.Chamber.Error(
			"failed to create plan due to dependency resolution error",
			err,
		)
	}

	dependencyOrder, err := p.GetDependencyOrder()
	if err != nil {
		return renovator.Chamber.Error(
			"failed to create plan due to dependency ordering error",
			err,
		)
	}

	decorIDsByGraphNodeIDs := make(
		map[int64]string,
		len(p.GraphNodeIDsByDecorIDs),
	)
	for decorID, graphNodeID := range p.GraphNodeIDsByDecorIDs {
		decorIDsByGraphNodeIDs[graphNodeID] = decorID
	}

	for _, graphNodeID := range dependencyOrder {
		decorID, _ := decorIDsByGraphNodeIDs[int64(graphNodeID)]
		p.DependencyOrder = append(p.DependencyOrder, decorID)
	}

	return nil
}

// ResolveDependency adds decorID and its dependencies to the Plan if not
// already present, and connects it to parentNode if provided.
func (p *Plan) ResolveDependency(
	renovator *Renovator,
	decorID string,
	parentNode *PlanNode,
) error {
	// If the decor is already in the plan, just connect it to the parent
	// (dependent) node if provided.
	if existingNode, exists := p.PlanNodesByDecorIDs[decorID]; exists {
		if parentNode != nil {
			existingNode.Dependents = append(
				existingNode.Dependents,
				parentNode.DecorID,
			)
			p.DependencyGraph.SetEdge(simple.Edge{
				F: simple.Node(parentNode.GraphNodeID),
				T: simple.Node(existingNode.GraphNodeID),
			})
		}
		return nil
	}

	chamber := renovator.Chamber
	spec, _ := chamber.Blueprint[decorID]
	if spec == nil {
		spec = NewVars()
	}

	decor, decorDef, err := chamber.Renovator.CreateDecor(decorID, spec)
	if err != nil {
		return chamber.Error(
			fmt.Sprintf("failed to create decor %q", decorID),
			err,
		)
	}

	planNode := &PlanNode{
		GraphNodeID:  int64(len(p.GraphNodeIDsByDecorIDs)),
		DecorID:      decorID,
		DecorDef:     decorDef,
		Decor:        decor,
		Dependencies: decor.Dependencies(),
		Dependents:   []string{},
	}
	p.PlanNodesByDecorIDs[decorID] = planNode
	p.GraphNodeIDsByDecorIDs[decorID] = planNode.GraphNodeID

	if parentNode != nil {
		planNode.Dependents = append(planNode.Dependents, parentNode.DecorID)
	}

	p.DependencyGraph.AddNode(simple.Node(planNode.GraphNodeID))
	if parentNode != nil {
		p.DependencyGraph.SetEdge(simple.Edge{
			F: simple.Node(parentNode.GraphNodeID),
			T: simple.Node(planNode.GraphNodeID),
		})
	}

	if err := p.ResolveDependencies(
		renovator,
		planNode.Dependencies,
		planNode,
	); err != nil {
		return err
	}

	return nil
}

// ResolveDependencies adds each decor ID and its dependencies to the Plan.
func (p *Plan) ResolveDependencies(
	renovator *Renovator,
	decorIDs []string,
	parentNode *PlanNode,
) error {
	for _, decorID := range decorIDs {
		if err := p.ResolveDependency(
			renovator,
			decorID,
			parentNode,
		); err != nil {
			return err
		}
	}

	return nil
}

// GetDependencyOrder returns graph node IDs with dependencies before
// dependents.
func (p *Plan) GetDependencyOrder() ([]int, error) {
	orderedNodes, err := topo.SortStabilized(
		p.DependencyGraph,
		func(nodes []graph.Node) {
			sort.Slice(nodes, func(i, j int) bool {
				return nodes[i].ID() < nodes[j].ID()
			})
		},
	)
	if err != nil {
		var unorderable topo.Unorderable
		if errors.As(err, &unorderable) {
			return nil, fmt.Errorf("cyclic dependency detected: %w", err)
		}

		return nil, fmt.Errorf("sort dependency graph: %w", err)
	}

	// Sort returns "from -> to". Our edges are "dependent -> dependency",
	// so reverse to get dependencies before dependents.
	order := make([]int, 0, len(orderedNodes))
	for i := len(orderedNodes) - 1; i >= 0; i-- {
		node := orderedNodes[i]
		if node == nil {
			return nil, fmt.Errorf("invalid dependency order: nil node")
		}
		order = append(order, int(node.ID()))
	}

	return order, nil
}
