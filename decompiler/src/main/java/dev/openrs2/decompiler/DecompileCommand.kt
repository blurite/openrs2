package dev.openrs2.decompiler

import com.github.ajalt.clikt.core.CliktCommand
import dev.openrs2.deob.util.Module

fun main(args: Array<String>) = DecompileCommand().main(args)

class DecompileCommand : CliktCommand(name = "decompile") {
    override fun run() {
        val decompiler = Decompiler(Module.all)
        decompiler.run()
    }
}
