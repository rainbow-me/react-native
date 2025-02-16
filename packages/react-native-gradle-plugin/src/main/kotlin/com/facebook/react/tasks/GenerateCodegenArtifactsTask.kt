/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.tasks

import com.facebook.react.utils.JsonUtils
import com.facebook.react.utils.windowsAwareCommandLine
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory

abstract class GenerateCodegenArtifactsTask : Exec() {

  @get:Internal abstract val reactNativeDir: DirectoryProperty

  @get:Internal abstract val codegenDir: DirectoryProperty

  @get:Internal abstract val generatedSrcDir: DirectoryProperty

  @get:InputFile abstract val packageJsonFile: RegularFileProperty

  @get:Input abstract val nodeExecutableAndArgs: ListProperty<String>

  @get:Input abstract val codegenJavaPackageName: Property<String>

  @get:Input abstract val libraryName: Property<String>

  // We're keeping this just to fire a warning at the user should they use the `reactRoot` property.
  @get:Internal abstract val deprecatedReactRoot: DirectoryProperty

  @get:InputFile
  val combineJsToSchemaCli: Provider<RegularFile> =
      codegenDir.file("lib/cli/combine/combine-js-to-schema-cli.js")

  @get:InputFile
  val generatedSchemaFile: Provider<RegularFile> = generatedSrcDir.file("schema.json")

  @get:OutputDirectory val generatedJavaFiles: Provider<Directory> = generatedSrcDir.dir("java")

  @get:OutputDirectory val generatedJniFiles: Provider<Directory> = generatedSrcDir.dir("jni")

  override fun exec() {
    checkForDeprecatedProperty()

    val (resolvedLibraryName, resolvedCodegenJavaPackageName) = resolveTaskParameters()
    setupCommandLine(resolvedLibraryName, resolvedCodegenJavaPackageName)
    super.exec()
  }

  private fun checkForDeprecatedProperty() {
    if (deprecatedReactRoot.isPresent) {
      project.logger.error(
          """
        ********************************************************************************
        The `reactRoot` property is deprecated and will be removed in 
        future versions of React Native. The property is currently ignored.
        
        You should instead use either:
        - [root] to point to your root project (where the package.json lives)
        - [reactNativeDir] to point to the NPM package of react native.
        
        You should be fine by just removing the `reactRoot` line entirely from 
        your build.gradle file. Otherwise a valid configuration would look like:
        
        react {
            root = rootProject.file('..')
            reactNativeDir = rootProject.file('../node_modules/react-native')
        }
        ********************************************************************************
      """
              .trimIndent())
    }
  }

  internal fun resolveTaskParameters(): Pair<String, String> {
    val parsedPackageJson =
        if (packageJsonFile.isPresent && packageJsonFile.get().asFile.exists()) {
          JsonUtils.fromCodegenJson(packageJsonFile.get().asFile)
        } else {
          null
        }
    val resolvedLibraryName = parsedPackageJson?.codegenConfig?.name ?: libraryName.get()
    val resolvedCodegenJavaPackageName =
        parsedPackageJson?.codegenConfig?.android?.javaPackageName ?: codegenJavaPackageName.get()
    return resolvedLibraryName to resolvedCodegenJavaPackageName
  }

  internal fun setupCommandLine(libraryName: String, codegenJavaPackageName: String) {
    commandLine(
        windowsAwareCommandLine(
            *nodeExecutableAndArgs.get().toTypedArray(),
            reactNativeDir.file("scripts/generate-specs-cli.js").get().asFile.absolutePath,
            "--platform",
            "android",
            "--schemaPath",
            generatedSchemaFile.get().asFile.absolutePath,
            "--outputDir",
            generatedSrcDir.get().asFile.absolutePath,
            "--libraryName",
            libraryName,
            "--javaPackageName",
            codegenJavaPackageName))
  }
}
