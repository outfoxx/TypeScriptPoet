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

/** A generated property declaration. */
class PropertySpec
private constructor(
  builder: Builder
) : Taggable(builder.tags.toImmutableMap()) {

  val name = builder.name
  val type = builder.type
  val javaDoc = builder.javaDoc.build()
  val decorators = builder.decorators.toImmutableList()
  val modifiers = builder.modifiers.toImmutableSet()
  val initializer = builder.initializer
  val optional = builder.optional

  internal fun emit(
    codeWriter: CodeWriter,
    implicitModifiers: Set<Modifier>,
    asStatement: Boolean = false,
    withInitializer: Boolean = true,
    compactOptionalAllowed: Boolean = false,
    scope: List<String>
  ) {
    codeWriter.emitJavaDoc(javaDoc, scope)
    codeWriter.emitDecorators(decorators, false, scope)
    codeWriter.emitModifiers(modifiers, implicitModifiers)
    codeWriter.emitCode(
      CodeBlock.of(
        "%L${if (optional && compactOptionalAllowed) "?" else ""}: %T${if (optional && !compactOptionalAllowed) " | undefined" else ""}",
        name,
        type
      ),
      scope
    )
    if (withInitializer && initializer != null) {
      codeWriter.emit(" = ")
      codeWriter.emitCode(CodeBlock.of("%[%L%]", initializer), scope)
    }
    if (asStatement) {
      codeWriter.emit(";\n")
    }
  }

  override fun toString() = buildCodeString { emit(this, emptySet(), scope = emptyList()) }

  fun toBuilder(): Builder {
    val bldr = Builder(name, type, optional)
      .addJavadoc(javaDoc)
      .addDecorators(decorators)
      .addModifiers(*modifiers.toTypedArray())
    initializer?.let { bldr.initializer(it) }

    return bldr
  }

  class Builder internal constructor(
    internal val name: String,
    internal val type: TypeName,
    internal var optional: Boolean
  ) : Taggable.Builder<Builder>() {

    internal val javaDoc = CodeBlock.builder()
    internal val decorators = mutableListOf<DecoratorSpec>()
    internal val modifiers = mutableListOf<Modifier>()
    internal var initializer: CodeBlock? = null

    fun addJavadoc(format: String, vararg args: Any) = apply {
      javaDoc.add(format, *args)
    }

    fun addJavadoc(block: CodeBlock) = apply {
      javaDoc.add(block)
    }

    fun addDecorators(decoratorSpecs: Iterable<DecoratorSpec>) = apply {
      decorators += decoratorSpecs
    }

    fun addDecorator(decoratorSpec: DecoratorSpec) = apply {
      decorators += decoratorSpec
    }

    fun addModifiers(vararg modifiers: Modifier) = apply {
      this.modifiers += modifiers
    }

    fun optional(optional: Boolean) = apply {
      this.optional = optional
    }

    fun initializer(format: String, vararg args: Any?) = initializer(
      CodeBlock.of(format, *args)
    )

    fun initializer(codeBlock: CodeBlock) = apply {
      check(this.initializer == null) { "initializer was already set" }
      this.initializer = codeBlock
    }

    fun build() = PropertySpec(this)
  }

  companion object {

    @JvmStatic
    fun builder(name: String, type: TypeName, optional: Boolean = false, vararg modifiers: Modifier): Builder {
      return Builder(name, type, optional).addModifiers(*modifiers)
    }
  }
}
