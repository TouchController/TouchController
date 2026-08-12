package top.fifthlight.intellij.expectactual

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPackage
import com.intellij.psi.impl.light.LightMethodBuilder
import com.intellij.psi.impl.light.LightPsiClassBuilder
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

class ExpectFactoryElementFinder : PsiElementFinder() {
    override fun findClass(
        qualifiedName: String,
        scope: GlobalSearchScope,
    ): PsiClass? = findFactoryClasses(qualifiedName, scope).firstOrNull()

    override fun findClasses(
        qualifiedName: String,
        scope: GlobalSearchScope,
    ): Array<PsiClass> = findFactoryClasses(qualifiedName, scope)

    private fun findFactoryClasses(
        qualifiedName: String,
        scope: GlobalSearchScope,
    ): Array<PsiClass> {
        if (!qualifiedName.endsWith(Consts.FACTORY_SUFFIX)) {
            return emptyArray()
        }

        val project = scope.project ?: return emptyArray()
        val expectQualifiedName = qualifiedName.removeSuffix(Consts.FACTORY_SUFFIX)
        if (expectQualifiedName.isEmpty() || expectQualifiedName.endsWith(".")) {
            return emptyArray()
        }

        val expectClass = JavaPsiFacade.getInstance(project)
            .findClass(expectQualifiedName, scope) ?: return emptyArray()

        val factoryInterface = expectClass.innerClasses.firstOrNull { inner ->
            inner.modifierList?.findAnnotation(Consts.EXPECT_FACTORY_ANNOTATION_FQN) != null
        } ?: return emptyArray()

        val packageName = expectQualifiedName.substringBeforeLast('.', "")
        val factoryClass = tryCreateFactoryClass(expectClass, factoryInterface, packageName, project)
            ?: return emptyArray()
        return arrayOf(factoryClass)
    }

    override fun getClasses(
        psiPackage: PsiPackage,
        scope: GlobalSearchScope,
    ): Array<PsiClass> {
        val project = psiPackage.project
        val packageName = psiPackage.qualifiedName
        val packagePrefix = if (packageName.isNotEmpty()) "$packageName." else ""

        val result = mutableListOf<PsiClass>()
        FileBasedIndex.getInstance().processAllKeys(ExpectFactoryIndexExtension.KEY, { fqn ->
            if (!fqn.startsWith(packagePrefix)) return@processAllKeys true

            val expectClass = JavaPsiFacade.getInstance(project).findClass(fqn, scope)
                ?: return@processAllKeys true
            val factoryInterface = expectClass.innerClasses.firstOrNull { inner ->
                inner.modifierList?.findAnnotation(Consts.EXPECT_FACTORY_ANNOTATION_FQN) != null
            } ?: return@processAllKeys true

            val factoryClass = tryCreateFactoryClass(expectClass, factoryInterface, packageName, project)
                ?: return@processAllKeys true
            result.add(factoryClass)
            true
        }, project)
        return result.toTypedArray()
    }

    private fun tryCreateFactoryClass(
        expectClass: PsiClass,
        factoryInterface: PsiClass,
        packageName: String,
        project: Project,
    ): SyntheticFactoryClass? {
        if (!expectClass.isInterface) {
            return null
        }

        val simpleName = expectClass.name ?: return null
        val factoryQualifiedName = if (packageName.isNotEmpty()) {
            "$packageName.$simpleName${Consts.FACTORY_SUFFIX}"
        } else {
            "$simpleName${Consts.FACTORY_SUFFIX}"
        }

        return SyntheticFactoryClass(
            context = expectClass,
            factoryQualifiedName = factoryQualifiedName,
            name = "$simpleName${Consts.FACTORY_SUFFIX}",
            expectClass = expectClass,
            factoryInterface = factoryInterface,
            project = project,
        )
    }

    private class SyntheticFactoryClass(
        context: PsiElement,
        private val factoryQualifiedName: String,
        name: String,
        private val expectClass: PsiClass,
        factoryInterface: PsiClass,
        project: Project,
    ) : LightPsiClassBuilder(context, name) {

        init {
            setNavigationElement(factoryInterface)
            modifierList.addModifier("public")

            val elementFactory = JavaPsiFacade.getInstance(project).elementFactory
            val returnType = elementFactory.createType(expectClass)

            for (factoryMethod in factoryInterface.methods) {
                val methodBuilder = LightMethodBuilder(manager, factoryMethod.name)
                    .setMethodReturnType(returnType)
                    .addModifier("public")
                    .addModifier("static")
                    .setContainingClass(this)
                    .also { method ->
                        method.setNavigationElement(factoryMethod)
                    }

                for (parameter in factoryMethod.parameterList.parameters) {
                    methodBuilder.addParameter(parameter.name, parameter.type)
                }

                addMethod(methodBuilder)
            }
        }

        override fun getQualifiedName(): String = factoryQualifiedName

        override fun getContainingFile(): PsiFile? = expectClass.containingFile

        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (this::class.java != other::class.java) return false
            val other = other as SyntheticFactoryClass
            return other.expectClass == this.expectClass && other.factoryQualifiedName == this.factoryQualifiedName
        }

        override fun hashCode(): Int {
            var result = factoryQualifiedName.hashCode()
            result = 31 * result + expectClass.hashCode()
            return result
        }
    }
}
