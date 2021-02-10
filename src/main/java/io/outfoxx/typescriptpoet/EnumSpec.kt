/*
 * Copyright 2017 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.typescriptpoet


/** A generated `enum` declaration. */
class EnumSpec
private constructor(
   builder: Builder
) : Taggable(builder.tags.toImmutableMap()) {
  val name = builder.name
  val javaDoc = builder.javaDoc.build()
  val modifiers = builder.modifiers.toImmutableSet()
  val constants = builder.constants.toImmutableMap()

  internal fun emit(codeWriter: CodeWriter, scope: List<String>) {

    codeWriter.emitJavaDoc(javaDoc, scope)
    codeWriter.emitModifiers(modifiers, setOf())
    codeWriter.emitCode(CodeBlock.of("enum %L {\n", name), scope)

    codeWriter.indent()
    val i = constants.entries.iterator()
    while (i.hasNext()) {
      val constant = i.next()
      codeWriter.emitCode(CodeBlock.of("%L", constant.key), scope)
      constant.value?.let {
        codeWriter.emitCode(" = ")
        codeWriter.emitCode(it, scope)
      }
      if (i.hasNext()) {
        codeWriter.emit(",\n")
      }
      else {
        codeWriter.emit("\n")
      }
    }

    codeWriter.unindent()
    codeWriter.emit("}\n")
  }

  private val hasNoBody: Boolean
    get() = constants.isEmpty()

  fun toBuilder(): Builder {
    val builder = Builder(name)
    builder.javaDoc.add(javaDoc)
    builder.modifiers += modifiers
    builder.constants += constants
    return builder
  }

  class Builder
  internal constructor(
     val name: String,
     val constants: MutableMap<String, CodeBlock?> = mutableMapOf()
  ) : Taggable.Builder<Builder>() {
    internal val javaDoc = CodeBlock.builder()
    internal val modifiers = mutableListOf<Modifier>()

    fun addJavadoc(format: String, vararg args: Any) = apply {
      javaDoc.add(format, *args)
    }

    fun addJavadoc(block: CodeBlock) = apply {
      javaDoc.add(block)
    }

    fun addModifiers(vararg modifiers: Modifier) = apply {
      modifiers.forEach { require(it == Modifier.EXPORT || it == Modifier.DECLARE) }
      this.modifiers += modifiers
    }

    fun addConstant(name: String, initializer: String? = null) =
       addConstant(name, initializer?.let { CodeBlock.of(it) })

    fun addConstant(name: String, initializer: CodeBlock?) = apply {
      require(name.isName) { "not a valid enum constant: $name" }
      constants.put(name, initializer)
    }

    fun build(): EnumSpec {
      return EnumSpec(this)
    }
  }

  companion object {

    @JvmStatic
    fun builder(name: String) = Builder(name)

    @JvmStatic
    fun builder(name: TypeName) = Builder(
       name.reference(null, emptyList()))

  }

}
