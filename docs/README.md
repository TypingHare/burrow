# Burrow Documentation

## Overview and Notations

**Burrow** is a command-line application that manages a collection of **chambers**, each is a command-line application. If we want to use the `bar` command in the `foo` chamber, then through Burrow, we execute

```bash
burrow foo bar
```

Because Burrow contains a lot of abstract concepts, in this documentation, we will use concise math notations to express the relationship between all these concepts. We denote $B$ as a burrow, and $C$ as a chamber. In essence, a burrow is nothing but a chamber launcher with some environment variables, therefore, we can represent a burrow with a 2-tuple containing an environment variable store $\mathcal{E}$ and a sequence of chambers:

$$
B = (\mathcal{E}, \{C_i\}_1^n).
$$

A chamber consists of a set of **decorations** $D$, with a **blueprint** $\mathcal{B}$ containing the entire configurations to install the decorations. The construction of a chamber is simply the installations for all decorations it depends on. Therefore, a chamber can be represented as:

$$
C = (\mathcal{B}, \{ D_i \}_1^n).
$$

A **decoration factory** is a function that can produce a decoration instance with a **raw spec** $\mathcal{R}$ and the chamber that creates it. Let $f_d$ denote a decoration factory that produces a decoration named $d$. Each decoration has a **spec** $\mathcal{S}$ parsed from a **raw spec**, hence,

$$
f_d(R, C) = D^S,
$$

where $D^S$ represents a decoration with spec $S$.

A **carton** $A$ is a set of decoration factories:

$$
A = \{ f_d \mid d \text{ are different } \}
$$

We use $f_d \in A$ to represent that $f_d$ is in the carton. Let $A_a$ represent a carton named $a$. Then, the **ID** of a decoration instance created by $f_d \in A_a$ is $I = d . '@' . a$. For example, if decoration named `core` is in the carton `burrow`, then its ID is `core@burrow`.

Burrow contains a single builtin carton named `burrow`, which contains a set of builtin decorations. Among them, the `core` decoration is the most important decoration that most chamber needs to depend on (developers can create a custom decoration to replace `core` for performance improvement). Another important decoration is `clutter`, which is usually only depended on by the **root chamber**. It has a `cartons` entry in its spec and provides a `build` command to install the cartons specified. Users can build a Burrow executable containing all the cartons using this command:

```bash
burrow . build
```

Here, `.` is the name of the root chamber. Instead of being loaded on runtime, cartons are statically linked to the Burrow executable, allowing the least runtime performance loss.

Burrow registers all decoration factories in cartons to a **warehouse** ($W$) on startup. Each decoration in the warehouse is associated with an ID $I$:

$$
W = \{ f_I \mid I \text{ are difference } \}
$$

In a chamber blueprint, there is a special entry `dependencies`, whose value is a list of decoration IDs. Other entries in the blueprint map decoration IDs to raw specs. When a chamber is constructed (also known as **dug**), a component responsible for installing dependencies for the chamber called **renovator**, will install these decorations with corresponding decoration factories and raw specs in the blueprint. It is ensured that _with the same blueprint yields the same chamber deterministically_.

A decoration can also include dependencies in their spec. Renovator will resolve dependencies recursively and create a dependency graph. The digging will fail if a cycle is found in the graph.

Each chamber instance has a **handler**, which is a function that handles incoming command (an array of arguments). When Burrow receives a command, it handles it by treating the first argument as the chamber name and passing the rest arguments to the handler of the corresponding chamber.

The following is a review for the important terms in Burrow:

- **Burrow**: a collection of chamber instances.
- **Chamber**: a collection of decorations with a handler handling incoming commands.
- **Decoration**: a component in a chamber that provides certain feature for a chamber.
- **Decoration factory**: a function that produces a decoration.
- **Carton**: a collection of decoration factories.
- **Warehouse**: a component in a burrow instance that collects all decoration factories from cartons.
- **Blueprint**: configurations for a chamber.
- **Raw Spec**: (unserialized) configurations for a decoration in a chamber.
- **Spec**: (serialized) configurations for a decoration in a chamber.
- **Renovator**: a component in a chamber that resolves decoration installations.
- **Decoration ID**: a combination of the name of a decoration and the name of the carton it belongs to.
- **Digging**: the process of creating a chamber instance.
- **Root Chamber**: a special chamber that manages all chambers including itself. Recall that Burrow is non-functional.

The following are some terms that are not mentioned in this section but will be mentioned in other sections:

- **Architect**: a component in a chamber that manages all chambers.
- **Burying**: the process of destroying a chamber instance and removing it from memory.

## Table of Contents
