### Version

In Burrow, a version contains three components: the **major version**, the **minor version**, and the **patch version**, which is similar to Go versioning. However, the major version must be identical to the year of the first release. The minor version is incremented when there are significant changes, and the patch version is incremented for bug fixes and minor improvements.

Burrow doesn't guarantee backward compatibility between major versions and minor versions. However, Burrow will not release a new minor version very frequently, and it will provide a migration guide for each new minor version.

Cartons that have the same major version and minor version are considered compatible. For example, if the version of Burrow is `2026.1.5`, then `2026.1.1` is considered compatible, while `2026.2.1` is not. Therefore, carton developers can use a separate internal version for each minor version. However, Burrow only cares about the version in the carton's metadata.
