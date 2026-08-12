package top.fifthlight.intellij.expectactual

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UInjectionHost

class ActualConstructorMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    private val UMethod.uastParentWithStatic: UElement?
        get() {
            fun getKotlinCompanionParent(): UElement? {
                if (lang !is KotlinLanguage) return null
                findAnnotation("kotlin.jvm.JvmStatic") ?: return null
                val method = sourcePsi as? KtFunction ?: return null
                val enclosingClass = method.containingClassOrObject ?: return null
                if (enclosingClass.modifierList?.hasModifier(KtTokens.COMPANION_KEYWORD) != true) {
                    return null
                }
                val companionParent = enclosingClass.containingClassOrObject ?: return null
                return UastFacade.convertWithParent<UClass>(companionParent)
            }

            return getKotlinCompanionParent() ?: uastParent
        }

    override fun collectSlowLineMarkers(elements: List<PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        for (element in elements) {
            val identifierOwner = getUParentForIdentifier(element) ?: continue
            val method = identifierOwner as? UMethod ?: continue
            val annotation = method.findAnnotation("top.fifthlight.mergetools.api.ActualConstructor") ?: continue

            val enclosingClass = method.uastParentWithStatic as? UClass ?: continue
            val actualImplAnnotation =
                enclosingClass.findAnnotation("top.fifthlight.mergetools.api.ActualImpl") ?: continue
            val valueExpression =
                actualImplAnnotation.findAttributeValue("value") as? UClassLiteralExpression ?: continue
            val targetType = valueExpression.type ?: continue
            val expectClass = PsiUtil.resolveClassInType(targetType) ?: continue

            val factoryClass = expectClass.innerClasses.firstOrNull {
                it.modifierList?.findAnnotation(Consts.EXPECT_FACTORY_ANNOTATION_FQN) != null
            } ?: continue

            val annotationValue = annotation.findAttributeValue("value")
            val targetMethodName = (annotationValue as? UInjectionHost)?.evaluateToString()
                ?.takeIf { it.isNotEmpty() } ?: method.name

            val targetMethod = factoryClass.findMethodsByName(targetMethodName, true).firstOrNull() ?: continue

            result.add(
                NavigationGutterIconBuilder.create(Assets.Actual)
                    .setTarget(targetMethod)
                    .setTooltipText("Go to factory method")
                    .setAlignment(GutterIconRenderer.Alignment.LEFT)
                    .createLineMarkerInfo(element)
            )
        }
    }
}
