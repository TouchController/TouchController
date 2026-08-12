/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.intellij.textitem

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.kotlin.idea.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class TextItemFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean,
    ): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtDotQualifiedExpression) {
                    val selector = element.selectorExpression
                    if (selector is KtSimpleNameExpression) {
                        val placeholder = resolveTextItem(selector)
                        if (placeholder != null) {
                            descriptors.add(
                                FoldingDescriptor(element.node, element.textRange, null, placeholder),
                            )
                        }
                    }
                }
                super.visitElement(element)
            }
        })
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        val expression = node.psi as? KtDotQualifiedExpression ?: return null
        val selector = expression.selectorExpression as? KtSimpleNameExpression ?: return null
        return resolveTextItem(selector)
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true

    private fun resolveTextItem(expression: KtSimpleNameExpression): String? {
        val target = expression.references.firstOrNull()?.resolve() ?: return null
        val declaration = target as? KtCallableDeclaration ?: return null
        val annotation = declaration.findAnnotation(TEXT_ITEM_FQN) ?: return null
        val valueArgument = annotation.valueArgumentList?.arguments?.firstOrNull { arg ->
            val name = arg.getArgumentName()?.asName?.asString()
            name == null || name == VALUE_PARAMETER_NAME
        } ?: return null
        val stringExpression =
            valueArgument.getArgumentExpression() as? KtStringTemplateExpression ?: return null
        return stringExpression.text
    }

    companion object {
        private val TEXT_ITEM_FQN = FqName("top.fifthlight.touchcontroller.common.annotations.TextItem")
        private const val VALUE_PARAMETER_NAME = "value"
    }
}
