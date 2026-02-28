package kernel

import (
	"errors"
	"fmt"
	"reflect"
	"sort"

	"gonum.org/v1/gonum/graph"
	"gonum.org/v1/gonum/graph/simple"
	"gonum.org/v1/gonum/graph/topo"
)

// Renovator resolves, orders, and stores a Chamber's decorations.
type Renovator struct {
	// chamber is the Chamber managed by the Renovator.
	chamber *Chamber

	// graph stores dependency edges from dependent to dependency.
	graph *simple.DirectedGraph

	// orderedDecorationIDs stores decoration IDs in initialization order.
	orderedDecorationIDs []string

	// orderedDecorations stores decorations in initialization order.
	orderedDecorations []DecorationInstance

	// decorationsByID stores decorations by decoration ID.
	decorationsByID map[string]DecorationInstance

	// decorationsByType stores decorations by concrete type.
	decorationsByType map[reflect.Type]DecorationInstance

	// graphNodeIDsByDecorationID maps decoration IDs to graph node IDs.
	graphNodeIDsByDecorationID map[string]int

	// resolvingDecorationIDSet tracks decorations currently being resolved.
	resolvingDecorationIDSet map[string]struct{}
}

// NewRenovator returns a Renovator for chamber.
func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		chamber:                    chamber,
		graph:                      simple.NewDirectedGraph(),
		orderedDecorationIDs:       []string{},
		orderedDecorations:         []DecorationInstance{},
		decorationsByID:            make(map[string]DecorationInstance),
		decorationsByType:          make(map[reflect.Type]DecorationInstance),
		graphNodeIDsByDecorationID: make(map[string]int),
		resolvingDecorationIDSet:   make(map[string]struct{}),
	}
}

// Chamber returns the Chamber managed by the Renovator.
func (r *Renovator) Chamber() *Chamber {
	return r.chamber
}

// Graph returns the dependency graph of the Chamber's decorations.
func (r *Renovator) Graph() *simple.DirectedGraph {
	return r.graph
}

// OrderedDecorationIDs returns decoration IDs in initialization order.
func (r *Renovator) OrderedDecorationIDs() []string {
	return r.orderedDecorationIDs
}

// OrderedDecorations returns decorations in initialization order.
func (r *Renovator) OrderedDecorations() []DecorationInstance {
	return r.orderedDecorations
}

// DecorationsByID returns a map of decorations by their decoration ID.
func (r *Renovator) DecorationsByID() map[string]DecorationInstance {
	return r.decorationsByID
}

// DecorationsByType returns a map of decorations by their concrete type.
func (r *Renovator) DecorationsByType() map[reflect.Type]DecorationInstance {
	return r.decorationsByType
}

// GetDecoration returns the decoration for decorationID.
func (r *Renovator) GetDecoration(
	decorationID string,
) (DecorationInstance, bool) {
	decoration, ok := r.decorationsByID[decorationID]
	return decoration, ok
}

// GetDecorationByType returns the decoration for decorationType.
func (r *Renovator) GetDecorationByType(
	decorationType reflect.Type,
) (DecorationInstance, bool) {
	if decorationType == nil {
		return nil, false
	}

	decoration, ok := r.decorationsByType[decorationType]
	return decoration, ok
}

// CreateDecoration builds the decoration for decorationID and rawSpec.
func (r *Renovator) CreateDecoration(
	decorationID string,
	rawSpec RawSpec,
) (DecorationInstance, error) {
	decorationFactory, err := r.chamber.Burrow().
		Warehouse().
		GetDecorationFactory(decorationID)
	if err != nil {
		return nil, r.chamber.Error(
			fmt.Sprintf(
				"failed to get decoration factory for %q",
				decorationID,
			),
			err,
		)
	}

	decoration, err := decorationFactory(r.chamber, rawSpec)
	if err != nil {
		return nil, r.chamber.Error(
			fmt.Sprintf(
				"failed to create decoration for dependency %q",
				decorationID,
			),
			err,
		)
	}

	return decoration, nil
}

// resolveRootDependencies resolves root decorations, builds the dependency
// graph, and records decorations in initialization order.
func (r *Renovator) resolveRootDependencies(decorationIDs []string) error {
	r.resetResolutionState()

	err := r.resolveDependencies(decorationIDs, -1)
	if err != nil {
		return r.chamber.Error("failed to resolve root dependencies", err)
	}

	orderedNodeIDs, err := r.getDependencyOrder()
	if err != nil {
		return r.chamber.Error("failed to get dependency order", err)
	}

	orderedDecorationIDs := make([]string, 0, len(orderedNodeIDs))
	for _, nodeID := range orderedNodeIDs {
		if nodeID < 0 || nodeID >= len(r.orderedDecorationIDs) {
			return fmt.Errorf("invalid dependency node ID: %d", nodeID)
		}

		decorationID := r.orderedDecorationIDs[nodeID]
		orderedDecorationIDs = append(orderedDecorationIDs, decorationID)

		decoration := r.decorationsByID[decorationID]
		if decoration == nil {
			return fmt.Errorf("invalid dependency ID: %s", decorationID)
		}

		r.orderedDecorations = append(r.orderedDecorations, decoration)
	}

	r.orderedDecorationIDs = orderedDecorationIDs

	return nil
}

// resolveDependencies recursively resolves decorationIDs.
//
// parentNodeID is the dependent node for the current resolution step. Each edge
// in the graph points from a dependent decoration to one of its dependencies.
func (r *Renovator) resolveDependencies(
	decorationIDs []string,
	parentNodeID int,
) error {
	for _, decorationID := range decorationIDs {
		nodeID, exists := r.graphNodeIDsByDecorationID[decorationID]
		if exists {
			if parentNodeID != -1 {
				r.graph.SetEdge(simple.Edge{
					F: simple.Node(parentNodeID),
					T: simple.Node(nodeID),
				})
			}

			_, resolving := r.resolvingDecorationIDSet[decorationID]
			if resolving {
				return fmt.Errorf(
					"cyclic dependency detected involving %q",
					decorationID,
				)
			}

			continue
		}

		rawSpec, err := r.chamber.Blueprint().GetRawSpec(decorationID)
		if err != nil {
			return r.chamber.Error(
				fmt.Sprintf(
					"failed to get raw spec for decoration %q",
					decorationID,
				),
				err,
			)
		}

		decoration, err := r.CreateDecoration(decorationID, rawSpec)
		if err != nil {
			return r.chamber.Error(
				fmt.Sprintf(
					"failed to create decoration %q",
					decorationID,
				),
				err,
			)
		}

		r.orderedDecorationIDs = append(r.orderedDecorationIDs, decorationID)

		nodeID = len(r.orderedDecorationIDs) - 1
		r.graphNodeIDsByDecorationID[decorationID] = nodeID
		r.graph.AddNode(simple.Node(nodeID))
		if parentNodeID != -1 {
			r.graph.SetEdge(simple.Edge{
				F: simple.Node(parentNodeID),
				T: simple.Node(nodeID),
			})
		}
		r.resolvingDecorationIDSet[decorationID] = struct{}{}

		if err := r.resolveDependencies(
			decoration.Dependencies(),
			nodeID,
		); err != nil {
			return err
		}
		delete(r.resolvingDecorationIDSet, decorationID)

		r.decorationsByID[decorationID] = decoration
		r.decorationsByType[reflect.TypeOf(decoration)] = decoration
	}

	return nil
}

// getDependencyOrder returns graph node IDs in dependency-first order.
func (r *Renovator) getDependencyOrder() ([]int, error) {
	orderedNodes, err := topo.SortStabilized(
		r.graph,
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

		return nil, fmt.Errorf("failed to sort dependency graph: %w", err)
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

// resetResolutionState clears dependency resolution state before a fresh run.
func (r *Renovator) resetResolutionState() {
	r.graph = simple.NewDirectedGraph()
	r.orderedDecorationIDs = []string{}
	r.orderedDecorations = []DecorationInstance{}
	r.decorationsByID = make(map[string]DecorationInstance)
	r.decorationsByType = make(map[reflect.Type]DecorationInstance)
	r.graphNodeIDsByDecorationID = make(map[string]int)
	r.resolvingDecorationIDSet = make(map[string]struct{})
}
