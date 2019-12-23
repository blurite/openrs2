package dev.openrs2.decompiler

import com.google.common.io.ByteStreams
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider
import org.jetbrains.java.decompiler.main.extern.IResultSaver
import java.io.Closeable
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.jar.Manifest

class DecompilerIo(private val destination: (String) -> Path) : IBytecodeProvider, IResultSaver, Closeable {
    private val inputJars = mutableMapOf<String, JarFile>()

    @Throws(IOException::class)
    override fun getBytecode(externalPath: String, internalPath: String?): ByteArray {
        if (internalPath == null) {
            throw UnsupportedOperationException()
        }

        val jar = inputJars.computeIfAbsent(externalPath) {
            JarFile(it)
        }

        jar.getInputStream(jar.getJarEntry(internalPath)).use {
            return ByteStreams.toByteArray(it)
        }
    }

    override fun saveFolder(path: String) {
        // ignore
    }

    override fun copyFile(source: String, path: String, entryName: String) {
        throw UnsupportedOperationException()
    }

    override fun saveClassFile(
        path: String,
        qualifiedName: String,
        entryName: String,
        content: String,
        mapping: IntArray
    ) {
        throw UnsupportedOperationException()
    }

    override fun createArchive(path: String, archiveName: String, manifest: Manifest?) {
        // ignore
    }

    override fun saveDirEntry(path: String, archiveName: String, entryName: String) {
        // ignore
    }

    override fun copyEntry(source: String, path: String, archiveName: String, entry: String) {
        // ignore
    }

    override fun saveClassEntry(
        path: String,
        archiveName: String,
        qualifiedName: String,
        entryName: String,
        content: String
    ) {
        val p = destination(archiveName).resolve(entryName)
        Files.createDirectories(p.parent)
        Files.newBufferedWriter(p).use {
            it.write(content)
        }
    }

    override fun closeArchive(path: String, archiveName: String) {
        // ignore
    }

    @Throws(IOException::class)
    override fun close() {
        for (jar in inputJars.values) {
            jar.close()
        }
    }
}
