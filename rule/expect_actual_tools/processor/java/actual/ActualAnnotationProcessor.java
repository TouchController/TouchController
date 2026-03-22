package top.fifthlight.mergetools.processor.java.actual;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.javapoet.*;
import top.fifthlight.mergetools.api.ActualConstructor;
import top.fifthlight.mergetools.api.ActualImpl;
import top.fifthlight.mergetools.api.ExpectFactory;
import top.fifthlight.mergetools.processor.ActualData;
import top.fifthlight.mergetools.processor.java.util.Util;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({"top.fifthlight.mergetools.api.ActualImpl"})
public class ActualAnnotationProcessor extends AbstractProcessor {
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
    }

    private Optional<TypeElement> findImplementedInterface(TypeElement type, String expectedInterfaceQualifiedName) {
        for (var interfaceItem : type.getInterfaces()) {
            if (interfaceItem.getKind() != TypeKind.DECLARED) {
                continue;
            }
            var declaredType = (DeclaredType) interfaceItem;
            var declaredElement = (TypeElement) declaredType.asElement();
            if (declaredElement.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            if (declaredElement.getQualifiedName().toString().equals(expectedInterfaceQualifiedName)) {
                return Optional.of(declaredElement);
            }
        }

        var superclass = type.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            var superclassElement = (TypeElement) ((DeclaredType) superclass).asElement();
            if (!superclassElement.getQualifiedName().toString().equals("java.lang.Object")) {
                var result = findImplementedInterface(superclassElement, expectedInterfaceQualifiedName);
                if (result.isPresent()) {
                    return result;
                }
            }
        }

        return Optional.empty();
    }

    private ActualData generateFactoryClass(String expectPackage,
                                            String expectClassName,
                                            String actualPackage,
                                            String actualClassName,
                                            String actualBinaryName,
                                            ActualData.Constructor[] constructors) {
        var interfaceClass = ClassName.get(expectPackage, expectClassName);
        var factoryClass = interfaceClass.nestedClass("Factory");

        var implClass = ClassName.get(actualPackage, actualClassName);
        var factoryImplClass = ClassName.get(actualPackage, actualClassName + "FactoryImpl");
        var typeSpecBuilder = TypeSpec.classBuilder(factoryImplClass)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(factoryClass);

        for (var constructor : constructors) {
            var methodBuilder = MethodSpec.methodBuilder(constructor.name())
                    .addAnnotation(Override.class)
                    .returns(interfaceClass)
                    .addModifiers(Modifier.PUBLIC);
            for (var parameter : constructor.parameters()) {
                methodBuilder.addParameter(Util.getJavaTypeName(parameter.type()), parameter.name());
            }
            var statement = Arrays.stream(constructor.parameters())
                    .map(p -> CodeBlock.of("$L", p.name()))
                    .collect(CodeBlock.joining(", "));
            switch (constructor.type()) {
                case CONSTRUCTOR -> methodBuilder.addStatement("return new $T($L)", implClass, statement);
                case STATIC_METHOD ->
                        methodBuilder.addStatement("return $T.$L($L)", implClass, constructor.name(), statement);
            }
            typeSpecBuilder.addMethod(methodBuilder.build());
        }

        var javaFile = JavaFile.builder(actualPackage, typeSpecBuilder.build()).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate factory implementation: " + e.getMessage());
            return null;
        }

        var filePath = "META-INF/services/" + expectPackage + "." + expectClassName + "$Factory";
        try {
            var resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", filePath);
            try (var writer = resource.openWriter()) {
                writer.write(factoryImplClass.canonicalName());
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate factory service file: " + ex.getMessage());
            return null;
        }
        return new ActualData(actualBinaryName, "L" + factoryImplClass.reflectionName().replace('.', '/') + ";", constructors);
    }

    private boolean generateActualManifest(String expectFullQualifiedName, ActualData actualData) {
        try {
            var mapper = new ObjectMapper();
            var filePath = "META-INF/actuals/" + expectFullQualifiedName + ".json";
            var resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", filePath);

            try (var writer = resource.openWriter()) {
                mapper.writeValue(writer, actualData);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate JSON file: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        outer:
        for (var element : roundEnv.getElementsAnnotatedWith(ActualImpl.class)) {
            if (!(element instanceof TypeElement type)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ActualImpl can only be applied to a type");
                continue;
            }

            var actualImplAnnotation = element.getAnnotation(ActualImpl.class);
            String expectClassFullQualifiedName;
            try {
                expectClassFullQualifiedName = actualImplAnnotation.value().getCanonicalName();
            } catch (MirroredTypeException ex) {
                if (ex.getTypeMirror().getKind() != TypeKind.DECLARED) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ActualImpl's value can only be a type");
                    continue;
                }
                var declaredType = (DeclaredType) ex.getTypeMirror();
                var declaredElement = (TypeElement) declaredType.asElement();
                expectClassFullQualifiedName = declaredElement.getQualifiedName().toString();
            }
            var expectClassQualifiedName = Objects.requireNonNull(expectClassFullQualifiedName);

            var expectElementOptional = findImplementedInterface(type, expectClassQualifiedName);

            if (expectElementOptional.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Class " + type.getQualifiedName() + " annotated with @ActualImpl must implement an interface named " + expectClassFullQualifiedName);
                continue;
            }
            var expectElement = expectElementOptional.get();

            TypeElement expectFactoryElement = null;
            for (var expectEnclosedElement : expectElement.getEnclosedElements()) {
                if (expectEnclosedElement.getKind() != ElementKind.INTERFACE) {
                    continue;
                }
                var declaredElement = (TypeElement) expectEnclosedElement;
                if (declaredElement.getAnnotation(ExpectFactory.class) != null) {
                    expectFactoryElement = declaredElement;
                    break;
                }
            }

            var constructors = new ArrayList<ActualData.Constructor>();
            for (var enclosedElement : element.getEnclosedElements()) {
                var actualConstructorAnnotation = enclosedElement.getAnnotation(ActualConstructor.class);
                if (actualConstructorAnnotation == null) {
                    continue;
                }
                var isConstructor = enclosedElement.getKind() == ElementKind.CONSTRUCTOR;
                var isMethod = enclosedElement.getKind() == ElementKind.METHOD;
                var isStatic = enclosedElement.getModifiers().contains(Modifier.STATIC);
                ActualData.Constructor.Type constructorType;
                if (isConstructor) {
                    constructorType = ActualData.Constructor.Type.CONSTRUCTOR;
                } else if (isMethod && isStatic) {
                    constructorType = ActualData.Constructor.Type.STATIC_METHOD;
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ActualConstructor can only be applied to a constructor or a static method for type " + type.getQualifiedName());
                    continue outer;
                }
                var constructorName = actualConstructorAnnotation.value();
                if (constructorName.isEmpty()) {
                    if (isConstructor) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ActualConstructor must have name set on constructors for type " + type.getQualifiedName());
                        continue outer;
                    }
                    constructorName = enclosedElement.getSimpleName().toString();
                }

                var method = (ExecutableElement) enclosedElement;
                var parameters = method.getParameters()
                        .stream()
                        .map(parameter -> new ActualData.Constructor.Parameter(
                                Util.getInternalTypeName(elementUtils, parameter.asType()),
                                parameter.getSimpleName().toString()
                        ))
                        .toArray(ActualData.Constructor.Parameter[]::new);

                String returnTypeName;
                if ("<init>".equals(method.getSimpleName().toString())) {
                    returnTypeName = Util.getInternalTypeName(elementUtils, expectElement.asType());
                } else {
                    var returnType = method.getReturnType();
                    if (returnType.getKind() != TypeKind.DECLARED) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ActualConstructor's return type must be a declared type for type " + type.getQualifiedName());
                        continue outer;
                    }
                    returnTypeName = Util.getInternalTypeName(elementUtils, returnType);
                }

                constructors.add(new ActualData.Constructor(constructorType, constructorName, parameters, returnTypeName));
            }
            var actualBinaryName = Util.getInternalTypeName(elementUtils, element.asType());

            var expectPackage = elementUtils.getPackageOf(expectElement);
            if (expectPackage.isUnnamed()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ExpectFactory can only be applied to member class in named package for type " + type.getQualifiedName());
                continue;
            }
            var expectPackageName = expectPackage.getQualifiedName().toString();
            var expectClassName = expectElement.getSimpleName().toString();

            var actualPackage = elementUtils.getPackageOf(element);
            if (actualPackage.isUnnamed()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ActualImpl can only be applied to a named package for type " + type.getQualifiedName());
                continue;
            }
            var actualPackageName = actualPackage.getQualifiedName().toString();
            var actualClassName = element.getSimpleName().toString();

            var actualData = generateFactoryClass(expectPackageName, expectClassName, actualPackageName, actualClassName, actualBinaryName, constructors.toArray(ActualData.Constructor[]::new));
            if (actualData == null) {
                continue;
            }
            if (!generateActualManifest(expectClassFullQualifiedName, actualData)) {
                continue;
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "packageName: " + actualPackageName + ", expectElement: " + expectElement.getQualifiedName() + ", expectFactoryElement: " + expectFactoryElement);
        }
        return true;
    }
}
