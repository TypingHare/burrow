package burrow.kernel.furniture

import burrow.kernel.config.Config

data class Blueprint(val config: Config, val furnishingIds: Set<String>)