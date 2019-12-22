package dev.openrs2.asm

import org.objectweb.asm.tree.*

data class MemberRef(val owner: String, val name: String, val desc: String) {
    constructor(clazz: ClassNode, field: FieldNode) : this(clazz.name, field.name, field.desc)
    constructor(clazz: ClassNode, method: MethodNode) : this(clazz.name, method.name, method.desc)
    constructor(fieldInsn: FieldInsnNode) : this(fieldInsn.owner, fieldInsn.name, fieldInsn.desc)
    constructor(methodInsn: MethodInsnNode) : this(methodInsn.owner, methodInsn.name, methodInsn.desc)
    constructor(owner: String, desc: MemberDesc) : this(owner, desc.name, desc.desc)

    override fun toString(): String {
        return "$desc $owner.$name"
    }
}