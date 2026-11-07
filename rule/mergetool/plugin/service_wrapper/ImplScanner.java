package top.fifthlight.mergetools.merger.plugin.servicewrapper;

import org.objectweb.asm.*;
import top.fifthlight.mergetools.api.MergeServiceMethod;

import java.util.ArrayList;
import java.util.List;

/// Scans one implementation class for `@MergeServiceMethod` methods.
public final class ImplScanner {
    private ImplScanner() {
    }

    public record Result(String internalName, int access, boolean hasPublicNoArgConstructor,
                         List<MethodItem> annotatedMethods) {
    }

    public static Result scan(byte[] classBytes) {
        var visitor = new Visitor();
        var reader = new ClassReader(classBytes);
        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
        return new Result(reader.getClassName(), reader.getAccess(), visitor.hasPublicNoArgConstructor,
                List.copyOf(visitor.annotatedMethods));
    }

    private static final class Visitor extends ClassVisitor {
        private final ArrayList<MethodItem> annotatedMethods = new ArrayList<>();
        boolean hasPublicNoArgConstructor = false;

        private Visitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals("<init>")) {
                if ((access & Opcodes.ACC_PUBLIC) != 0 && descriptor.equals("()V")) {
                    hasPublicNoArgConstructor = true;
                }
                return null;
            }
            if (name.equals("<clinit>")
                    || (access & Opcodes.ACC_PUBLIC) == 0
                    || (access & (Opcodes.ACC_STATIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC)) != 0) {
                return null;
            }
            return new MethodVisitor(Opcodes.ASM9) {
                private boolean annotated = false;

                @Override
                public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                    if (annotationDescriptor.equals(MergeServiceMethod.DESCRIPTOR)) {
                        annotated = true;
                    }
                    return null;
                }

                @Override
                public void visitEnd() {
                    if (annotated) {
                        annotatedMethods.add(new MethodItem(name, descriptor));
                    }
                }
            };
        }
    }
}
