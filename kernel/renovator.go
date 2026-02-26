package kernel

import (
	"errors"
	"fmt"
	"sort"

	"gonum.org/v1/gonum/graph"
	"gonum.org/v1/gonum/graph/simple"
	"gonum.org/v1/gonum/graph/topo"
)

// Renovator is responsible for managing decorations in a chamber.
type Renovator struct {
	// Chamber is the chamber that the renovator is managing.
	chamber *Chamber

	// DecorationMap is a map of decoration IDs to their corresponding
	// decorations.
	decorationMap map[string]DecorationInstance

	// DependencyGraph is a directed graph that represents the dependencies
	// between decorations. Each node in the graph represents a decoration, and
	// each directed edge from node A to node B indicates that decoration A
	// depends on decoration B.
	dependencyGraph *simple.DirectedGraph

	// DependencyIDs is a list of decoration IDs that the renovator needs to
	// resolve. This list is used to keep track of the decorations that need to
	// be resolved in order to ensure that all dependencies are properly
	// handled.
	dependencyIDs []string

	dependencyOrder []DecorationInstance
}

// NewRenovator creates a new Renovator for the given chamber.
func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		chamber:         chamber,
		decorationMap:   make(map[string]DecorationInstance),
		dependencyGraph: simple.NewDirectedGraph(),
		dependencyIDs:   []string{},
	}
}

// Decorations returns the map of decoration IDs to their corresponding
// decorations.
func (r *Renovator) Decorations() map[string]DecorationInstance {
	return r.decorationMap
}

// GetDecoration returns the Decoration instance for the given decoration ID.
func (r *Renovator) GetDecoration(
	decorationID string,
) (DecorationInstance, bool) {
	decoration, ok := r.decorationMap[decorationID]
	return decoration, ok
}

// CreateDecoration creates a Decoration instance for the given decoration ID
// and raw specification.
func (r *Renovator) CreateDecoration(
	decorationID string,
	rawSpec RawSpec,
) (DecorationInstance, error) {
	decorationFactory, err := r.chamber.Burrow().
		Warehouse().
		GetDecorationFactory(decorationID)
	if err != nil {
		return nil, NewChamberError(
			r.chamber.Name(),
			fmt.Sprintf("get decoration for dependency '%s'", decorationID),
			err,
		)
	}

	decoration, err := decorationFactory(r.chamber, rawSpec)
	if err != nil {
		return nil, NewChamberError(
			r.chamber.Name(),
			fmt.Sprintf("create decoration for dependency '%s'", decorationID),
			err,
		)
	}

	return decoration, nil
}

// resolveRootDependencies resolves the root dependencies of the chamber and
// builds the dependency graph. It takes a list of dependency IDs and returns an
// error if any of the dependencies cannot be resolved or if there is a cyclic
// dependency.
func (r *Renovator) resolveRootDependencies(dependencyIDs []string) error {
	err := r.resolveDependencies(dependencyIDs, -1)
	if err != nil {
		return err
	}

	dependencyNodeIDOrder, err := r.getDependencyOrder()
	if err != nil {
		return err
	}

	for _, dependencyNodeID := range dependencyNodeIDOrder {
		if dependencyNodeID < 0 || dependencyNodeID >= len(r.dependencyIDs) {
			return fmt.Errorf(
				"invalid dependency node ID: %d",
				dependencyNodeID,
			)
		}

		dependencyID := r.dependencyIDs[dependencyNodeID]
		dependency := r.decorationMap[dependencyID]
		if dependency == nil {
			return fmt.Errorf(
				"invalid dependency ID: %s",
				dependencyID,
			)
		}

		r.dependencyOrder = append(r.dependencyOrder, dependency)
	}

	for _, dependency := range r.dependencyOrder {
		dependency.Assemble()
	}
	for _, dependency := range r.dependencyOrder {
		dependency.Launch()
	}

	return nil
}

// resolveDependencies recursively resolves the dependencies for the given list
// of dependency IDs. It adds the dependencies to the dependency graph and
// updates the decoration map. The parentNodeID is used to track the parent node
// in the dependency graph for the current dependencies being resolved. If there
// is a cyclic dependency, it returns an error.
func (r *Renovator) resolveDependencies(
	dependencyIDs []string,
	parentNodeID int,
) error {
	for _, decorationID := range dependencyIDs {
		if _, exists := r.decorationMap[decorationID]; exists {
			continue
		}

		rawSpec, err := r.chamber.Blueprint().GetRawSpec(decorationID)
		if err != nil {
			return NewChamberError(
				r.chamber.name,
				fmt.Sprintf("get raw spec for dependency '%s'", decorationID),
				err,
			)
		}

		decoration, err := r.CreateDecoration(decorationID, rawSpec)
		if err != nil {
			return NewChamberError(
				r.chamber.name,
				fmt.Sprintf("create decoration '%s'", decorationID),
				err,
			)
		}

		r.dependencyIDs = append(r.dependencyIDs, decorationID)

		// Add the decoration as a node in the dependency graph.
		decorationNodeID := len(r.dependencyIDs) - 1
		r.dependencyGraph.AddNode(simple.Node(decorationNodeID))
		if parentNodeID != -1 {
			r.dependencyGraph.SetEdge(simple.Edge{
				F: simple.Node(parentNodeID),
				T: simple.Node(decorationNodeID),
			})
		}

		// Continue to resolve dependencies for the newly created decoration.
		if err := r.resolveDependencies(
			decoration.Dependencies(),
			decorationNodeID,
		); err != nil {
			return err
		}

		r.decorationMap[decorationID] = decoration
	}

	return nil
}

// getDependencyOrder returns the order of dependencies based on the dependency
// graph. It performs a topological sort on the dependency graph to determine
// the order in which the dependencies should be resolved. If there is a cyclic
// dependency, it returns an error.
func (r *Renovator) getDependencyOrder() ([]int, error) {
	orderedNodes, err := topo.SortStabilized(
		r.dependencyGraph,
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
