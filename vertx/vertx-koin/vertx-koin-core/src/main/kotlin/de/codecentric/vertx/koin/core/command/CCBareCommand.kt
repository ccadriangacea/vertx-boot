package de.codecentric.vertx.koin.core.command

import io.vertx.core.cli.annotations.Description
import io.vertx.core.cli.annotations.Name
import io.vertx.core.cli.annotations.Summary
import io.vertx.core.impl.launcher.commands.BareCommand
import io.vertx.core.spi.launcher.DefaultCommandFactory
import java.util.function.Supplier

@Name("cc-bare")
@Summary("Start an instance of vert.x")
@Description("This can be used to start vert.x without deploying any verticle")
class CCBareCommand : BareCommand() {
    override fun isClustered(): Boolean = false
}

class CCBareCommandFactory : DefaultCommandFactory<CCBareCommand>(CCBareCommand::class.java, Supplier { CCBareCommand() })
