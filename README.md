# Burrow

## Installation

Burrow can be installed on macOS and Linux. Please ensure that `Go 1.25+` and `Make 3.81+` is in your environment. Please also ensure that directory `~/.burrow` does not exist.

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/TypingHare/burrow/refs/heads/main/script/install.sh)"
```

## Overview

Imagine you are a rabbit living in a burrow with many chambers. You own one chamber where you can decorate it with decorations. The more decorations you have, the more functionalities your chamber gains. If you need more decorations, you can order them online, and they will arrive packaged in cartons. You can also chat and barter with other residents in their chambers.

**Burrow** is a command-line application that manages a collection of **chambers**, each of which is an independent command-line application. If we want to use the `bar` command in the `foo` chamber, we execute

```
burrow foo bar
```

Because Burrow contains many abstract concepts, we use concise mathematical notation to express the relationships between them. We denote $B$ as a burrow and $C$ as a chamber. In essence, a burrow is simply a chamber launcher with some environment variables. Therefore, we can represent a burrow as a 2-tuple containing an environment variable store $\mathcal{E}$ and a sequence of chambers:

$$
B = (\mathcal{E}, \{C_i\}_{i = 1}^n)
$$

A chamber consists of a set of **decorations** $D$, with a **blueprint** $\mathcal{B}$ that contains the complete configuration required to install the decorations. Constructing a chamber simply means installing all the decorations it depends on. Therefore, a chamber can be represented as:

$$
C = (\mathcal{B}, \{D_i\}_1^n)
$$

A **decoration factory** is a function that produces a decoration instance given a **raw spec** $\mathcal{R}$ and the chamber that creates it. Let $f_d$ denote a decoration factory that produces a decoration named $d$. Each decoration has a **spec** $\mathcal{S}$ parsed from the raw spec. Hence,

$$
f_d(R, C) = D^S
$$

where $D^S$ represents a decoration with spec $S$.

A carton $A$ is a sequence of decoration factories:

$$
A = \{ f_{d_i} \}_{i = 1}^n
$$

We write $f_d \in A$ to indicate that the decoration factory $f_d$ belongs to the carton $A$. Let $A_a$ denote a carton named $a$. The **ID** of a decoration instance created by $f_d \in A_a$ is defined as

$$
I = d . '@' . a
$$

For example, if a decoration named `core` is in the carton `github.com/TypingHare/burrow`, then its ID is `core@github.com/TypingHare/burrow`. The name of a carton always matches the Go module name without the major version.

Burrow includes a single built-in carton named `github.com/TypingHare/burrow`, which provides a set of built-in decorations. Among them, the `core` decoration is the most important, as most chambers depend on it. Another important decoration is `clutter`, which is usually depended on only by the **root chamber**. Its spec contains a `cartonNames` entry, and it provides a `build` command that installs the specified cartons.

Users can build a Burrow executable that includes all cartons using the following command:

```
burrow . burrow build
```

Here, `.` represents the name of the root chamber. Instead of being loaded at runtime, cartons are statically linked into the Burrow executable, ensuring minimal runtime performance overhead.

When Burrow starts, it registers all decoration factories from the cartons into a **warehouse** ($W$). Each decoration in the warehouse is associated with an ID $I$. Therefore, the warehouse can be represented as a function that maps IDs to their corresponding decoration factories:

$$
W : \mathcal{I} \to \mathcal{F}
$$

A chamber blueprint is a store that maps decoration IDs to their raw specs. When a chamber is constructed (also known as **dug**), a component called the **renovator** installs the dependencies required by the chamber. It installs all decorations defined in the blueprint using their corresponding raw specs and decoration factories.

A decoration may also declare dependencies in its spec. The renovator resolves these dependencies recursively and builds a dependency graph. The digging process fails if a cycle is detected in the graph.

Each chamber instance has a **handler**, which is a function that processes incoming commands (an array of arguments). When Burrow receives a command, it treats the first argument as the chamber name and passes the remaining arguments to the handler of the corresponding chamber.

Because the terms in Burrow are abstract, we provide the following comparison table between Burrow terms and general terms:

| Burrow Term        | Definition                                                           | General Term                    |
| ------------------ | -------------------------------------------------------------------- | :------------------------------ |
| Burrow             | a collection of chambers                                             | Workspace/Application Platform  |
| Chamber            | a collection of decorations                                          | Application                     |
| Decoration         | a component in a chamber that provides certain features              | Plugin                          |
| Decoration Factory | a function that produces a decoration                                | Plugin Factory                  |
| Carton             | a collection of decoration factories                                 | Plugin Bundle                   |
| Warehouse          | a component that collects all decoration factories from cartons      | Plugin Registry                 |
| Blueprint          | configurations for a chamber                                         | Configuration / Manifest        |
| Raw Spec           | unserialized configurations for a decoration                         | Rawed Configuration             |
| Spec               | serialized configurations for a decoration                           | Parsed Configuration            |
| Renovator          | a component that resolves decoration installations                   | Dependency Resolver / Installer |
| Decoration ID      | a combination of the name of a decoration and the name of the carton | Fully Qualified Plugin ID       |
| Dig                | the process of creating a chamber instance                           | Initialization                  |
| Root Chamber       | a special chamber that manages all chambers including itself         | Root Module / Main Application  |
| Architect          | a component that manages all chambers                                | Application Manager             |
| Bury               | the process of destroying a chamber instance                         | Shutdown                        |
