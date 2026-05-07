package top.fifthlight.mergetools.merger.plugin.expectactual;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.processor.ActualData;
import top.fifthlight.mergetools.processor.AspectData;
import top.fifthlight.mergetools.processor.ExpectData;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AspectProviderImplEntry implements MergeEntry {
    private final ExpectActualPluginContext context;
    private final String aspectProviderDescriptor;
    private final AspectData aspectData;
    public AspectProviderImplEntry(ExpectActualPluginContext context, String aspectProviderDescriptor, AspectData aspectData) {
        this.context = context;
        this.aspectProviderDescriptor = aspectProviderDescriptor;
        this.aspectData = aspectData;
    }

    private record MethodPair(String parameterTypes, String name) {
        public MethodPair(ExpectData.Constructor constructor) {
            this(Arrays.stream(constructor.parameters()).map(ExpectData.Constructor.Parameter::type).collect(Collectors.joining()), constructor.name());
        }

        public MethodPair(ActualData.Constructor constructor) {
            this(Arrays.stream(constructor.parameters()).map(ActualData.Constructor.Parameter::type).collect(Collectors.joining()), constructor.name());
        }
    }

    @Override
    public void write(OutputStream output) throws Exception {
        var className = ExpectActualUtils.descriptorNameToInternalName(aspectProviderDescriptor) + "Impl";
        var classWriter = new ClassWriter(0);
        var aspectProviderInternalName = ExpectActualUtils.descriptorNameToInternalName(aspectProviderDescriptor);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[]{aspectProviderInternalName});

        {
            var methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }

        var actualDataMap = context.getActualDataMap();
        for (var expectEntry : aspectData.expects()) {
            var expectBinaryName = expectEntry.interfaceName();
            var expectFqn = ExpectActualUtils.internalNameToFqn(ExpectActualUtils.descriptorNameToInternalName(expectBinaryName));
            var actualData = actualDataMap.get(expectFqn);
            if (actualData == null) {
                throw new IllegalStateException("Missing actual for aspect expect: " + expectFqn);
            }

            var actualClassName = ExpectActualUtils.descriptorNameToInternalName(actualData.implementationName());

            var expectConstructors = Arrays.stream(expectEntry.constructors()).collect(Collectors.toMap(
                    MethodPair::new,
                    constructor -> constructor,
                    (a, b) -> {
                        throw new IllegalStateException("Duplicate expect constructors: " + a + ", " + b);
                    }
            ));
            var actualConstructors = Arrays.stream(actualData.constructors()).collect(Collectors.toMap(
                    MethodPair::new,
                    constructor -> constructor,
                    (a, b) -> {
                        throw new IllegalStateException("Duplicate actual constructors: " + a + ", " + b);
                    }
            ));

            for (var expectConstructorPair : expectConstructors.entrySet()) {
                var methodPair = expectConstructorPair.getKey();
                var expectConstructor = expectConstructorPair.getValue();
                var actualConstructor = actualConstructors.get(methodPair);
                if (actualConstructor == null) {
                    throw new IllegalStateException("No actual constructor found for method pair " + methodPair);
                }

                var generatedMethodDescriptor = "(" + methodPair.parameterTypes() + ")" + expectBinaryName;
                var methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, expectConstructor.name(), generatedMethodDescriptor, null, null);
                methodVisitor.visitCode();

                ExpectActualUtils.generateDirectCallMethodBody(
                        methodVisitor, actualConstructor, expectConstructor,
                        actualClassName, methodPair.parameterTypes(), actualConstructor.returnType(), false
                );

                methodVisitor.visitEnd();
            }
        }

        classWriter.visitEnd();
        output.write(classWriter.toByteArray());
    }
}
