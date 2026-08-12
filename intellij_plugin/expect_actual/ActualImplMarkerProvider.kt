package top.fifthlight.intellij.expectactual

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.getUParentForIdentifier

class ActualImplMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: List<PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        for (element in elements) {
            val identifierOwner = getUParentForIdentifier(element) ?: continue
            val identifierClass = (identifierOwner as? UClass) ?: continue
            val annotation = identifierClass.findAnnotation(Consts.EXPECT_FACTORY_ANNOTATION_FQN) ?: continue
            val valueExpression = annotation.findAttributeValue("value") as? UClassLiteralExpression ?: continue
            val targetType = valueExpression.type ?: continue
            val targetClass = PsiUtil.resolveClassInType(targetType) ?: continue

            result.add(
                NavigationGutterIconBuilder.create(Assets.Actual)
                    .setTarget(targetClass)
                    .setTooltipText("Go to expect class")
                    .setAlignment(GutterIconRenderer.Alignment.LEFT)
                    .createLineMarkerInfo(element)
            )
        }
    }
}
