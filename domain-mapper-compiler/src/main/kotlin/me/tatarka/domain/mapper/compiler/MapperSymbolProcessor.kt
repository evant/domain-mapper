package me.tatarka.domain.mapper.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.withIndent
import me.tatarka.domain.mapper.annotations.Mapper

class MapperSymbolProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        for (file in resolver.getNewFiles()) {
            val declarations = file.declarations
                .filter { it.isAnnotationPresent(Mapper::class) }
                .filterIsInstance<KSClassDeclaration>()
                .toList()

            if (declarations.isEmpty()) continue

            FileSpec.builder(file.packageName.asString(), "${file.fileName}_Gen")
                .apply {
                    for (declaration in declarations) {
                        if (declaration.classKind != ClassKind.INTERFACE) {
                            logger.error("@Mapper must be declared on an interface", declaration)
                            continue
                        }

                        val typeResolver = declaration.typeParameters.toTypeParameterResolver()
                        val modifiers = declaration.modifiers.mapNotNull { it.toKModifier() }

                        val declarationType = declaration.toClassName()
                        val declarationImpl = "${declaration.simpleName.asString()}Impl"

                        addType(TypeSpec.objectBuilder(declarationImpl)
                            .addOriginatingKSFile(file)
                            .addSuperinterface(declarationType)
                            .addModifiers(KModifier.PRIVATE)
                            .apply {
                                for (function in declaration.getDeclaredFunctions().filter { it.isAbstract }) {
                                    val functionTypeResolver =
                                        function.typeParameters.toTypeParameterResolver(typeResolver)

                                    val receiverType = function.extensionReceiver?.toTypeName(functionTypeResolver)
                                    if (receiverType == null) {
                                        logger.error("function must take the source type as a receiver", function)
                                        continue
                                    }

                                    val returnType = function.returnType?.toTypeName(functionTypeResolver)
                                    if (returnType == null) {
                                        logger.error("function must return the target type", function)
                                        continue
                                    }

                                    addFunction(
                                        FunSpec.builder(function.simpleName.asString())
                                            .addModifiers(KModifier.OVERRIDE)
                                            .apply {
                                                for (param in function.parameters) {
                                                    addParameter(
                                                        param.name!!.asString(),
                                                        param.type.toTypeName(functionTypeResolver)
                                                    )
                                                }
                                            }
                                            .receiver(receiverType)
                                            .returns(returnType)
                                            .addCode(CodeBlock.builder()
                                                .add("return·%T(\n", returnType).withIndent {
                                                    for (param in (function.returnType!!.resolve().declaration as KSClassDeclaration)
                                                        .primaryConstructor!!.parameters) {
                                                        add("%1N·=·%1N,\n", param.name!!.asString())
                                                    }
                                                }
                                                .add(")")
                                                .build())
                                            .build()
                                    )
                                }
                            }
                            .build())

                        addFunction(
                            FunSpec.builder(declaration.simpleName.asString())
                                .addModifiers(modifiers)
                                .returns(declarationType)
                                .addCode("return %N", declarationImpl)
                                .build()
                        )
                    }
                }
                .build()
                .writeTo(codeGenerator, aggregating = false)
        }

        return emptyList()
    }
}

class MapperSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MapperSymbolProcessor(environment.logger, environment.codeGenerator)
    }
}