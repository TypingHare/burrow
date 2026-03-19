package kernel

import (
	"errors"
	"fmt"
	"sort"

	"gonum.org/v1/gonum/graph"
	"gonum.org/v1/gonum/graph/simple"
	"gonum.org/v1/gonum/graph/topo"
)

type PlanNode struct {
	GraphNodeID  int64
	DecorID      string
	DecorDef     DecorDef
	Decor        Decor
	Dependencies []string
	Dependents   []string
}

type Plan struct {
	DependencyGraph        *simple.DirectedGraph
	PlanNodesByDecorIDs    map[string]*PlanNode
	GraphNodeIDsByDecorIDs map[string]int64
	DependencyOrder        []string
}

func NewPlan() *Plan {
	return &Plan{
		DependencyGraph:        simple.NewDirectedGraph(),
		PlanNodesByDecorIDs:    make(map[string]*PlanNode),
		GraphNodeIDsByDecorIDs: make(map[string]int64),
		DependencyOrder:        []string{},
	}
}

func (p *Plan) CreatePlan(chamber *Chamber, decorIDs []string) error {
	err := p.ResolveDependencies(chamber, decorIDs, nil)
	if err != nil {
		return chamber.Error(
			"failed to create plan due to dependency resolution error",
			err,
		)
	}

	dependencyOrder, err := p.GetDependencyOrder()
	if err != nil {
		return chamber.Error(
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

func (p *Plan) ResolveDependency(
	chamber *Chamber,
	decorID string,
	parentNode *PlanNode,
) error {
	if _, exists := p.PlanNodesByDecorIDs[decorID]; exists {
		// This decor has already been resolved.
		return nil
	}

	rawSpec, err := chamber.Blueprint.GetRawSpec(decorID)
	if err != nil {
		return chamber.Error(
			fmt.Sprintf("failed to get raw spec for decor %q", decorID),
			err,
		)
	}

	decor, decorDef, err := chamber.Renovator.CreateDecor(decorID, rawSpec)
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

	// Continue to resolve dependencies for the newly created decor.
	if err := p.ResolveDependencies(
		chamber,
		planNode.Dependencies,
		planNode,
	); err != nil {
		return err
	}

	return nil
}

func (p *Plan) ResolveDependencies(
	chamber *Chamber,
	decorIDs []string,
	parentNode *PlanNode,
) error {
	for _, decorID := range decorIDs {
		if err := p.ResolveDependency(
			chamber,
			decorID,
			parentNode,
		); err != nil {
			return err
		}
	}

	return nil
}

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
