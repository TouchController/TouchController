package top.fifthlight.mergetools.processor.java.util;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Arrays;

public class Util {
    public static String getInternalTypeName(Elements elementUtils, TypeMirror typeMirror) {
        return switch (typeMirror.getKind()) {
            case BOOLEAN -> "Z";
            case BYTE -> "B";
            case CHAR -> "C";
            case SHORT -> "S";
            case INT -> "I";
            case LONG -> "J";
            case FLOAT -> "F";
            case DOUBLE -> "D";
            case VOID -> "V";
            case DECLARED -> {
                var declaredType = (DeclaredType) typeMirror;
                var typeElement = (TypeElement) declaredType.asElement();
                yield "L" + elementUtils.getBinaryName(typeElement).toString().replace('.', '/') + ";";
            }
            case ARRAY -> {
                var arrayType = (ArrayType) typeMirror;
                var componentType = arrayType.getComponentType();
                yield "[" + getInternalTypeName(elementUtils, componentType);
            }
            default -> throw new IllegalArgumentException("Unsupported TypeKind: " + typeMirror.getKind());
        };
    }

    public static TypeName getJavaTypeName(String internalTypeName) {
        if (internalTypeName == null || internalTypeName.isEmpty()) {
            return TypeName.VOID;
        }

        if (internalTypeName.startsWith("[")) {
            var componentTypeName = internalTypeName.substring(1);
            var componentJavaType = getJavaTypeName(componentTypeName);
            return ArrayTypeName.of(componentJavaType);
        }

        switch (internalTypeName) {
            case "Z":
                return TypeName.BOOLEAN;
            case "B":
                return TypeName.BYTE;
            case "C":
                return TypeName.CHAR;
            case "S":
                return TypeName.SHORT;
            case "I":
                return TypeName.INT;
            case "J":
                return TypeName.LONG;
            case "F":
                return TypeName.FLOAT;
            case "D":
                return TypeName.DOUBLE;
            case "V":
                return TypeName.VOID;
        }

        if (internalTypeName.startsWith("L") && internalTypeName.endsWith(";")) {
            var qualifiedName = internalTypeName.substring(1, internalTypeName.length() - 1).replace('/', '.');
            var lastDotIndex = qualifiedName.lastIndexOf('.');
            var packageName = lastDotIndex > 0 ? qualifiedName.substring(0, lastDotIndex) : "";
            var simpleNames = lastDotIndex > 0 ? qualifiedName.substring(lastDotIndex + 1) : qualifiedName;
            var simpleNameParts = simpleNames.split("\\$");

            return ClassName.get(packageName, simpleNameParts[0], Arrays.copyOfRange(simpleNameParts, 1, simpleNameParts.length));
        }

        throw new IllegalArgumentException("Cannot convert internal type name to JavaPoet type: " + internalTypeName);
    }
}
