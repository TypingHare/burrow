## Kernel

The kernel of Burrow is a Go package that provides the core entities required to construct and run a Burrow instance. Because Burrow is designed as a command-line tool, the kernel prioritizes efficiency. For this reason, all struct fields are exported, allowing direct access without additional abstraction layers.

This design introduces potential risks: if developers accidentally modify certain fields, they may corrupt the state of other chambers within the same burrow. Burrow therefore follows the principle of “developers responsible for security.” In practice, this means that decoration developers should carefully validate and handle all received commands to ensure safe behavior.

The Burrow kernel intentionally contains only the minimal set of components required for the system to function. As a result, developers may notice that many convenience or helper functions are not included in the kernel itself. Instead, these utilities are provided through decorations in the built-in carton burrow. For more information, see the documentation for the burrow built-in decorations: [burrow](../builtin-decorations/README.md) for more information.