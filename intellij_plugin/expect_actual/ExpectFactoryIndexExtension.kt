package top.fifthlight.intellij.expectactual

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.JavaTokenType
import com.intellij.psi.impl.source.tree.JavaElementType
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.lexer.KtTokens

class ExpectFactoryIndexExtension : FileBasedIndexExtension<String, String>() {
    companion object {
        val KEY: ID<String, String> = ID.create("top.fifthlight.intellij.expectactual.ExpectFactoryIndex")
        private const val VERSION = 2
        private val supportedFileTypes = setOf(JavaFileType.INSTANCE, KotlinFileType.INSTANCE)
    }

    override fun getName(): ID<String, String> = KEY

    override fun getVersion(): Int = VERSION

    override fun dependsOnFileContent(): Boolean = true

    override fun getIndexer(): DataIndexer<String, String, FileContent> = DataIndexer { content ->
        if ("ExpectFactory" !in content.contentAsText) return@DataIndexer emptyMap()
        ProgressManager.checkCanceled()

        val tree = (content as PsiDependentFileContent).lighterAST
        val result = mutableMapOf<String, String>()
        when (content.fileType) {
            JavaFileType.INSTANCE -> indexJava(tree, result)
            KotlinFileType.INSTANCE -> indexKotlin(tree, result)
        }
        result
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { file ->
        file.fileType in supportedFileTypes
    }
}

private fun indexJava(tree: LighterAST, result: MutableMap<String, String>) {
    val root = tree.root
    val packageName = javaPackageName(tree, root)

    for (cls in LightTreeUtil.getChildrenOfType(tree, root, JavaElementType.CLASS)) {
        if (LightTreeUtil.firstChildOfType(tree, cls, JavaTokenType.INTERFACE_KEYWORD) == null) continue
        val name = identifierText(tree, cls, JavaTokenType.IDENTIFIER) ?: continue
        val hasFactory = LightTreeUtil.getChildrenOfType(tree, cls, JavaElementType.CLASS).any { inner ->
            hasJavaExpectFactoryAnnotation(tree, inner)
        }
        if (!hasFactory) continue
        result[qualifiedName(packageName, name)] = ""
    }
}

private fun javaPackageName(tree: LighterAST, root: LighterASTNode): String {
    val packageStatement = LightTreeUtil.firstChildOfType(tree, root, JavaElementType.PACKAGE_STATEMENT) ?: return ""
    val reference = LightTreeUtil.firstChildOfType(tree, packageStatement, JavaElementType.JAVA_CODE_REFERENCE)
        ?: return ""
    return LightTreeUtil.toFilteredString(tree, reference, null)
}

private fun hasJavaExpectFactoryAnnotation(tree: LighterAST, node: LighterASTNode): Boolean {
    val modifierList = LightTreeUtil.firstChildOfType(tree, node, JavaElementType.MODIFIER_LIST) ?: return false
    for (child in tree.getChildren(modifierList)) {
        if (child.tokenType != JavaElementType.ANNOTATION) continue
        val reference = LightTreeUtil.firstChildOfType(tree, child, JavaElementType.JAVA_CODE_REFERENCE) ?: continue
        val simpleName = LightTreeUtil.toFilteredString(tree, reference, null).substringAfterLast('.')
        if (simpleName == Consts.EXPECT_FACTORY_ANNOTATION_SIMPLE) return true
    }
    return false
}

private fun indexKotlin(tree: LighterAST, result: MutableMap<String, String>) {
    val root = tree.root
    val packageName = kotlinPackageName(tree, root)

    for (cls in LightTreeUtil.getChildrenOfType(tree, root, KtNodeTypes.CLASS)) {
        if (LightTreeUtil.firstChildOfType(tree, cls, KtTokens.INTERFACE_KEYWORD) == null) continue
        val name = identifierText(tree, cls, KtTokens.IDENTIFIER) ?: continue
        val classBody = LightTreeUtil.firstChildOfType(tree, cls, KtNodeTypes.CLASS_BODY)
        val hasFactory = classBody != null &&
            LightTreeUtil.getChildrenOfType(tree, classBody, KtNodeTypes.CLASS).any { inner ->
                hasKotlinExpectFactoryAnnotation(tree, inner)
            }
        if (!hasFactory) continue
        result[qualifiedName(packageName, name)] = ""
    }
}

private fun kotlinPackageName(tree: LighterAST, root: LighterASTNode): String {
    val packageDirective = LightTreeUtil.firstChildOfType(tree, root, KtNodeTypes.PACKAGE_DIRECTIVE) ?: return ""
    val expression = LightTreeUtil.firstChildOfType(tree, packageDirective, KtNodeTypes.DOT_QUALIFIED_EXPRESSION)
        ?: LightTreeUtil.firstChildOfType(tree, packageDirective, KtNodeTypes.REFERENCE_EXPRESSION)
        ?: return ""
    return LightTreeUtil.toFilteredString(tree, expression, null)
}

private fun hasKotlinExpectFactoryAnnotation(tree: LighterAST, node: LighterASTNode): Boolean {
    val modifierList = LightTreeUtil.firstChildOfType(tree, node, KtNodeTypes.MODIFIER_LIST) ?: return false
    for (child in tree.getChildren(modifierList)) {
        if (child.tokenType != KtNodeTypes.ANNOTATION_ENTRY) continue
        val callee = LightTreeUtil.firstChildOfType(tree, child, KtNodeTypes.CONSTRUCTOR_CALLEE) ?: continue
        val typeReference = LightTreeUtil.firstChildOfType(tree, callee, KtNodeTypes.TYPE_REFERENCE) ?: continue
        val simpleName = LightTreeUtil.toFilteredString(tree, typeReference, null).substringAfterLast('.')
        if (simpleName == Consts.EXPECT_FACTORY_ANNOTATION_SIMPLE) return true
    }
    return false
}

private fun identifierText(tree: LighterAST, node: LighterASTNode, tokenType: IElementType): String? {
    val identifier = LightTreeUtil.firstChildOfType(tree, node, tokenType) ?: return null
    return (identifier as LighterASTTokenNode).text.toString()
}

private fun qualifiedName(packageName: String, name: String): String =
    if (packageName.isEmpty()) name else "$packageName.$name"
