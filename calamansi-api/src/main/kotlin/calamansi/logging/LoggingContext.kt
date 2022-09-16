package calamansi.logging

import calamansi.node.Node

interface LoggingContext {
    val Node.logger: Logger
}