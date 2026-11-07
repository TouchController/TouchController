package top.fifthlight.mergetools.merger.plugin.servicewrapper;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import top.fifthlight.mergetools.merger.api.MergeEntry;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/// A service wrapper for a type invisible to the merger (e.g. FML classes in MultiJAR): tries
/// instantiating implementations in order and delegates to the first success.
public class WrapperClassMergeEntry implements MergeEntry {
    private static final String ILLEGAL_STATE_INTERNAL_NAME = "java/lang/IllegalStateException";
    private static final String IMPL_FIELD_NAME = "impl";

    private final String classInternalName;
    private final String interfaceInternalName;
    private final ArrayList<String> implInternalNames;
    private final TreeSet<MethodItem> methods;

    public WrapperClassMergeEntry(String classInternalName, String interfaceInternalName, List<byte[]> implClasses) {
        this.classInternalName = classInternalName;
        this.interfaceInternalName = interfaceInternalName;
        this.implInternalNames = new ArrayList<>(implClasses.size());
        this.methods = new TreeSet<>();
        for (byte[] implClass : implClasses) {
            scanImpl(implClass);
        }
    }

    /// Validates the scanned implementation and registers its annotated methods.
    private void scanImpl(byte[] implClass) {
        var result = ImplScanner.scan(implClass);
        var implName = result.internalName();
        if (implInternalNames.contains(implName)) {
            throw new IllegalStateException("Duplicate implementation " + implName + " of " + interfaceInternalName);
        }
        if ((result.access() & Opcodes.ACC_PUBLIC) == 0) {
            throw new IllegalStateException("Implementation " + implName + " of " + interfaceInternalName + " is not public");
        }
        if ((result.access() & (Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT)) != 0) {
            throw new IllegalStateException("Implementation " + implName + " of " + interfaceInternalName + " is abstract");
        }
        if (!result.hasPublicNoArgConstructor()) {
            throw new IllegalStateException("Implementation " + implName + " of " + interfaceInternalName
                    + " has no public no-arg constructor, required to instantiate it from the wrapper");
        }
        implInternalNames.add(implName);
        methods.addAll(result.annotatedMethods());
    }

    @Override
    public void write(OutputStream output) throws Exception {
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        // public class Wrapper implements Interface {
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, classInternalName, null,
                "java/lang/Object", new String[]{interfaceInternalName});
        var interfaceDescriptor = internalNameToDescriptor(interfaceInternalName);
        // private final Interface impl;
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, IMPL_FIELD_NAME, interfaceDescriptor, null, null)
                .visitEnd();
        // public Wrapper() { ... }
        writeConstructor(writer, interfaceDescriptor);
        // public Ret name(args) { ... } for every annotated method.
        for (var item : methods) {
            writeDelegateMethod(writer, item, interfaceDescriptor);
        }
        // }
        writer.visitEnd();
        output.write(writer.toByteArray());
    }

    private void writeConstructor(ClassWriter writer, String interfaceDescriptor) {
        var methodVisitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        // super();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        // var failure = new IllegalStateException("No available implementation of ..."); local slot 1.
        methodVisitor.visitTypeInsn(Opcodes.NEW, ILLEGAL_STATE_INTERNAL_NAME);
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitLdcInsn("No available implementation of " + interfaceInternalName
                + " (tried: " + String.join(", ", implInternalNames) + ")");
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, ILLEGAL_STATE_INTERNAL_NAME, "<init>",
                "(Ljava/lang/String;)V", false);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 1);
        for (var implName : implInternalNames) {
            var tryStart = new Label();
            var tryEnd = new Label();
            var handler = new Label();
            methodVisitor.visitTryCatchBlock(tryStart, tryEnd, handler, "java/lang/Throwable");
            // try {
            methodVisitor.visitLabel(tryStart);
            // this.impl = new ImplN();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitTypeInsn(Opcodes.NEW, implName);
            methodVisitor.visitInsn(Opcodes.DUP);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, implName, "<init>", "()V", false);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, classInternalName, IMPL_FIELD_NAME, interfaceDescriptor);
            // return;
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitLabel(tryEnd);
            // } catch (Throwable t) {
            methodVisitor.visitLabel(handler);
            // failure.addSuppressed(t);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitInsn(Opcodes.SWAP);
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable", "addSuppressed",
                    "(Ljava/lang/Throwable;)V", false);
        }
        // throw failure;
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitInsn(Opcodes.ATHROW);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void writeDelegateMethod(ClassWriter writer, MethodItem item, String interfaceDescriptor) {
        var returnType = Type.getReturnType(item.descriptor());
        var methodVisitor = writer.visitMethod(Opcodes.ACC_PUBLIC, item.name(), item.descriptor(), null, null);
        methodVisitor.visitCode();
        // return impl.name(args);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, classInternalName, IMPL_FIELD_NAME, interfaceDescriptor);
        loadArguments(methodVisitor, item.descriptor());
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, interfaceInternalName, item.name(),
                item.descriptor(), true);
        methodVisitor.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private static void loadArguments(MethodVisitor methodVisitor, String methodDescriptor) {
        int slot = 1; // Slot 0 holds `this`.
        for (var type : Type.getArgumentTypes(methodDescriptor)) {
            methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ILOAD), slot);
            slot += type.getSize();
        }
    }

    private static String internalNameToDescriptor(String internalName) {
        return "L" + internalName + ";";
    }
}
