package dev.openrs2.deob.transform

import com.github.michaelbull.logging.InlineLogger
import dev.openrs2.asm.ClassVersionUtils
import dev.openrs2.asm.InsnMatcher
import dev.openrs2.asm.MemberRef
import dev.openrs2.asm.classpath.ClassPath
import dev.openrs2.asm.classpath.Library
import dev.openrs2.asm.toInternalClassName
import dev.openrs2.asm.transform.Transformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

class ClassLiteralTransformer : Transformer() {
    private val classForNameMethods = mutableListOf<MemberRef>()
    private var classLiterals = 0

    override fun preTransform(classPath: ClassPath) {
        classForNameMethods.clear()
        classLiterals = 0

        for (library in classPath.libraries) {
            for (clazz in library) {
                clazz.methods.removeIf { method ->
                    if (method.desc != "(Ljava/lang/String;)Ljava/lang/Class;") {
                        return@removeIf false
                    }

                    if (method.access and Opcodes.ACC_STATIC == 0) {
                        return@removeIf false
                    }

                    val match = CLASS_FOR_NAME_MATCHER.match(method).singleOrNull() ?: return@removeIf false

                    val invokestatic = match[1] as MethodInsnNode
                    if (
                        invokestatic.owner != "java/lang/Class" ||
                        invokestatic.name != "forName" ||
                        invokestatic.desc != "(Ljava/lang/String;)Ljava/lang/Class;"
                    ) {
                        return@removeIf false
                    }

                    classForNameMethods.add(MemberRef(clazz, method))
                    return@removeIf true
                }
            }
        }

        logger.info { "Identified Class::forName methods $classForNameMethods" }
    }

    override fun transformCode(classPath: ClassPath, library: Library, clazz: ClassNode, method: MethodNode): Boolean {
        for (match in CLASS_LITERAL_MATCHER.match(method)) {
            val getstatic1 = MemberRef(match[0] as FieldInsnNode)
            val putstatic = MemberRef(match[5] as FieldInsnNode)
            val getstatic2 = MemberRef(match[7] as FieldInsnNode)
            val invokestatic = MemberRef(match[3] as MethodInsnNode)

            if (getstatic1 != putstatic || putstatic != getstatic2) {
                continue
            }

            if (getstatic1.owner != clazz.name) {
                continue
            }

            if (invokestatic.owner != clazz.name) {
                continue
            }

            if (!classForNameMethods.contains(invokestatic)) {
                continue
            }

            for ((i, insn) in match.withIndex()) {
                if (i == 2) {
                    val ldc = insn as LdcInsnNode
                    ldc.cst = Type.getObjectType((ldc.cst as String).toInternalClassName())
                } else {
                    method.instructions.remove(insn)
                }
            }

            clazz.version = ClassVersionUtils.maxVersion(clazz.version, Opcodes.V1_5)
            clazz.fields.removeIf { it.name == getstatic1.name && it.desc == getstatic1.desc }

            classLiterals++
        }

        return false
    }

    override fun postTransform(classPath: ClassPath) {
        logger.info { "Updated $classLiterals class literals to Java 5 style" }
    }

    companion object {
        private val logger = InlineLogger()
        private val CLASS_FOR_NAME_MATCHER = InsnMatcher.compile(
            "^ALOAD INVOKESTATIC ARETURN ASTORE NEW DUP INVOKESPECIAL ALOAD INVOKEVIRTUAL ATHROW$"
        )
        private val CLASS_LITERAL_MATCHER = InsnMatcher.compile(
            "GETSTATIC IFNONNULL LDC INVOKESTATIC DUP PUTSTATIC GOTO GETSTATIC"
        )
    }
}