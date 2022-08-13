package calamansi.harness

import calamansi.ExecutionContext
import calamansi.Script

class MyScript : Script() {
    var int = 1

    context(ExecutionContext) override fun attached() {
        logger.info { "attached" }
        checkNotNull(owner.parent).addComponent(MyComponent::class)
    }

    context(ExecutionContext) override fun detached() {
        logger.info { "detached" }
        checkNotNull(owner.parent).removeComponent(MyComponent::class)
    }

    context(ExecutionContext) override fun update(delta: Float) {
        logger.info { "running delta: $delta" }
    }
}