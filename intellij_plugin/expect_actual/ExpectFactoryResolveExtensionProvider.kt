package top.fifthlight.intellij.expectactual

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.KaSpiExtensionPoint
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KaResolveExtension
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KaResolveExtensionFile
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KaResolveExtensionNavigationTargetsProvider
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KaResolveExtensionProvider
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

@OptIn(KaExperimentalApi::class, KaSpiExtensionPoint::class)
class ExpectFactoryResolveExtensionProvider : KaResolveExtensionProvider() {
    override fun provideExtensionsFor(module: KaModule): List<KaResolveExtension> =
        listOf(ExpectFactoryResolveExtension(module))
}

@OptIn(KaExperimentalApi::class, KaSpiExtensionPoint::class)
private class ExpectFactoryResolveExtension(private val module: KaModule) : KaResolveExtension() {
    private val files: List<KaResolveExtensionFile> by lazy { buildFiles() }

    override fun getKtFiles(): List<KaResolveExtensionFile> = files

    override fun getContainedPackages(): Set<FqName> = files.mapTo(mutableSetOf()) { it.getFilePackageName() }

    private fun buildFiles(): List<KaResolveExtensionFile> {
        val project = module.project
        val scope = module.contentScope
        val result = mutableListOf<KaResolveExtensionFile>()
        FileBasedIndex.getInstance().processAllKeys(ExpectFactoryIndexExtension.KEY, { fqn ->
            buildFileForFqn(fqn, project, scope)?.let(result::add)
            true
        }, project)
        return result
    }

    private fun buildFileForFqn(
        fqn: String,
        project: Project,
        scope: GlobalSearchScope,
    ): KaResolveExtensionFile? {
        val sourceFile = findKotlinSourceFile(fqn, project, scope) ?: return null
        val packageName = fqn.substringBeforeLast('.', "")
        val simpleName = fqn.substringAfterLast('.')

        val expectClass = sourceFile.declarations.filterIsInstance<KtClass>().firstOrNull { cls ->
            cls.name == simpleName && cls.isInterface()
        } ?: return null

        val factoryInterface = expectClass.declarations.filterIsInstance<KtClass>().firstOrNull { inner ->
            inner.isInterface() && hasExpectFactoryAnnotation(inner)
        } ?: return null

        return ExpectFactoryResolveExtensionFile(
            fileName = "${simpleName}${Consts.FACTORY_SUFFIX}.kt",
            packageFqn = if (packageName.isEmpty()) FqName.ROOT else FqName(packageName),
            classifierName = Name.identifier("${simpleName}${Consts.FACTORY_SUFFIX}"),
            expectSimpleName = simpleName,
            sourceFile = sourceFile,
            factoryInterface = factoryInterface,
        )
    }

    private fun findKotlinSourceFile(fqn: String, project: Project, scope: GlobalSearchScope): KtFile? {
        val files = FileBasedIndex.getInstance()
            .getContainingFiles(ExpectFactoryIndexExtension.KEY, fqn, scope)
        return files.firstNotNullOfOrNull { virtualFile ->
            if (virtualFile.fileType != KotlinFileType.INSTANCE) return@firstNotNullOfOrNull null
            PsiManager.getInstance(project).findFile(virtualFile) as? KtFile
        }
    }

    private fun hasExpectFactoryAnnotation(cls: KtClass): Boolean =
        cls.annotationEntries.any { it.shortName?.asString() == Consts.EXPECT_FACTORY_ANNOTATION_SIMPLE }
}

@OptIn(KaExperimentalApi::class, KaSpiExtensionPoint::class)
private class ExpectFactoryResolveExtensionFile(
    private val fileName: String,
    private val packageFqn: FqName,
    private val classifierName: Name,
    private val expectSimpleName: String,
    private val sourceFile: KtFile,
    private val factoryInterface: KtClass,
) : KaResolveExtensionFile() {
    override fun getFileName(): String = fileName

    override fun getFilePackageName(): FqName = packageFqn

    override fun getTopLevelClassifierNames(): Set<Name> = setOf(classifierName)

    override fun getTopLevelCallableNames(): Set<Name> = emptySet()

    override fun buildFileText(): String =
        buildFactoryText(packageFqn, expectSimpleName, sourceFile, factoryInterface)

    override fun createNavigationTargetsProvider(): KaResolveExtensionNavigationTargetsProvider =
        object : KaResolveExtensionNavigationTargetsProvider() {
            @KaSpiExtensionPoint
            override fun KaSession.getNavigationTargets(element: KtElement): Collection<PsiElement> = listOf(factoryInterface)
        }
}

private fun buildFactoryText(
    packageFqn: FqName,
    expectSimpleName: String,
    sourceFile: KtFile,
    factoryInterface: KtClass,
): String {
    val factoryInterfaceName = factoryInterface.name ?: "Factory"
    return buildString {
        if (!packageFqn.isRoot) {
            append("package ").append(packageFqn.asString()).append('\n')
        }
        if (sourceFile.importDirectives.isNotEmpty()) {
            append('\n')
            for (importDirective in sourceFile.importDirectives) {
                append(importDirective.text).append('\n')
            }
        }
        append('\n')
        append("class ").append(expectSimpleName).append(Consts.FACTORY_SUFFIX).append(" {\n")
        append("    @Suppress(\"unused\", \"UNUSED_PARAMETER\", \"UNUSED_VARIABLE\", \"UNNECESSARY_NOT_NULL_ASSERTION\")\n")
        append("    companion object {\n")
        append("        private val factoryImpl: ").append(expectSimpleName).append('.')
            .append(factoryInterfaceName).append(" = throw RuntimeException()\n\n")
        for (function in factoryInterface.declarations.filterIsInstance<KtNamedFunction>()) {
            val functionName = function.name ?: continue
            val parameterNames = mutableListOf<String>()
            val parameters = function.valueParameters.mapIndexed { index, parameter ->
                val parameterName = parameter.name ?: "p$index"
                parameterNames.add(parameterName)
                val parameterType = parameter.typeReference?.text ?: "Any"
                "$parameterName: $parameterType"
            }.joinToString(", ")
            val callArguments = parameterNames.joinToString(", ")
            append("        fun ").append(functionName).append('(').append(parameters).append("): ")
                .append(expectSimpleName).append(" = factoryImpl.").append(functionName)
                .append('(').append(callArguments).append(")!!\n")
        }
        append("    }\n")
        append("}\n")
    }
}
