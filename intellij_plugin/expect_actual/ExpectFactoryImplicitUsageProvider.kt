package top.fifthlight.intellij.expectactual

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

class ExpectFactoryImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitUsage(element: PsiElement): Boolean = isExpectFactoryRelated(element)

    override fun isImplicitRead(element: PsiElement): Boolean = false

    override fun isImplicitWrite(element: PsiElement): Boolean = false

    private fun isExpectFactoryRelated(element: PsiElement): Boolean {
        val factoryClass = when (element) {
            is PsiClass -> if (element.isInterface) element else null
            is PsiMethod -> element.containingClass?.takeIf { it.isInterface }
            else -> null
        } ?: return false
        return factoryClass.name == Consts.FACTORY_INTERFACE_NAME &&
            factoryClass.containingClass?.isInterface == true &&
            factoryClass.hasAnnotation(Consts.EXPECT_FACTORY_ANNOTATION_FQN)
    }
}
