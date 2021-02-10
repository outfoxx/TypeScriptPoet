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

package io.outfoxx.typescriptpoet.test

import io.outfoxx.typescriptpoet.CodeWriter
import io.outfoxx.typescriptpoet.Modifier
import io.outfoxx.typescriptpoet.TypeAliasSpec
import io.outfoxx.typescriptpoet.TypeName
import io.outfoxx.typescriptpoet.tag
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("TypeAliasSpec Tests")
class TypeAliasSpecTests {

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testBuilder = TypeAliasSpec.builder("Test", TypeName.STRING)
      .tag(5)
    val testSpec = testBuilder.build()

    assertThat(testBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testSpec.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Generates JavaDoc at before class definition")
  fun testGenJavaDoc() {
    val testAlias = TypeAliasSpec.builder("Integer", TypeName.NUMBER)
      .addJavadoc("this is a comment\n")
      .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out), scope = emptyList())

    assertThat(
      out.toString(),
      equalTo(
        """
            /**
             * this is a comment
             */
            type Integer = number;

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testAlias = TypeAliasSpec.builder("Integer", TypeName.NUMBER)
      .addModifiers(Modifier.EXPORT)
      .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out), scope = emptyList())

    assertThat(
      out.toString(),
      equalTo(
        """
            export type Integer = number;

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates simple alias")
  fun testSimpleAlias() {
    val testAlias = TypeAliasSpec.builder("Integer", TypeName.NUMBER)
      .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out), scope = emptyList())

    assertThat(
      out.toString(),
      equalTo(
        """
            type Integer = number;

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates generic alias")
  fun testGenericAlias() {
    val typeVar = TypeName.typeVariable(
      "A",
      TypeName.bound(
        TypeName.anyType("Test")
      )
    )
    val testAlias = TypeAliasSpec.builder(
      "StringMap",
      TypeName.mapType(
        TypeName.STRING, typeVar
      )
    )
      .addTypeVariable(typeVar)
      .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out), scope = emptyList())

    assertThat(
      out.toString(),
      equalTo(
        """
            type StringMap<A extends Test> = Map<string, A>;

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testAliasBldr = TypeAliasSpec.builder("Test", TypeName.NUMBER)
      .addJavadoc("this is a comment\n")
      .addModifiers(Modifier.EXPORT)
      .addTypeVariable(
        TypeName.typeVariable(
          "A",
          TypeName.bound(
            TypeName.anyType("Test")
          )
        )
      )
      .build()
      .toBuilder()

    assertThat(testAliasBldr.name, equalTo("Test"))
    assertThat(testAliasBldr.type, equalTo<TypeName>(TypeName.NUMBER))
    assertThat(testAliasBldr.javaDoc.formatParts, hasItems("this is a comment\n"))
    assertThat(testAliasBldr.modifiers, hasItems(Modifier.EXPORT))
    assertThat(testAliasBldr.typeVariables.map { it.name }, hasItems("A"))
  }
}
