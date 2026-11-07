package top.fifthlight.mergetools.merger.plugin.servicewrapper.test;

import org.junit.Test;
import top.fifthlight.mergetools.merger.plugin.servicewrapper.WrapperClassMergeEntry;
import top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture.BadImpl;
import top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture.GoodImpl;
import top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture.PartialImpl;
import top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture.TestService;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class WrapperClassMergeEntryTest {
    private static final String INTERFACE_INTERNAL_NAME = TestService.class.getName().replace('.', '/');
    private static final String WRAPPER_INTERNAL_NAME = INTERFACE_INTERNAL_NAME + "/wrapper/Wrapper";
    private static final String WRAPPER_CLASS_NAME = WRAPPER_INTERNAL_NAME.replace('/', '.');

    private static byte[] fixtureBytes(Class<?> clazz) throws Exception {
        var internalName = clazz.getName().replace('.', '/');
        try (var stream = clazz.getClassLoader().getResourceAsStream(internalName + ".class")) {
            assertNotNull("Fixture class " + internalName + " not found", stream);
            return stream.readAllBytes();
        }
    }

    private static WrapperClassMergeEntry entryOf(Class<?>... implClasses) throws Exception {
        var implBytes = new ArrayList<byte[]>();
        for (var clazz : implClasses) {
            implBytes.add(fixtureBytes(clazz));
        }
        return new WrapperClassMergeEntry(WRAPPER_INTERNAL_NAME, INTERFACE_INTERNAL_NAME, implBytes);
    }

    private static Class<?> defineWrapper(WrapperClassMergeEntry entry) throws Exception {
        var bytes = new ByteArrayOutputStream();
        entry.write(bytes);
        var loader = new ClassLoader(TestService.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (!name.equals(WRAPPER_CLASS_NAME)) {
                    throw new ClassNotFoundException(name);
                }
                return defineClass(name, bytes.toByteArray(), 0, bytes.size());
            }
        };
        return loader.loadClass(WRAPPER_CLASS_NAME);
    }

    @Test
    public void testSelectsFirstConstructibleImplementation() throws Exception {
        GoodImpl.doItCalled = false;
        var wrapper = defineWrapper(entryOf(BadImpl.class, GoodImpl.class));
        var instance = wrapper.getDeclaredConstructor().newInstance();

        assertEquals(42, wrapper.getMethod("getInt").invoke(instance));
        assertEquals(424242424242L, wrapper.getMethod("getLong").invoke(instance));
        wrapper.getMethod("doIt").invoke(instance);
        assertTrue(GoodImpl.doItCalled);

        // Only annotated methods are wrapped.
        var methodNames = Arrays.stream(wrapper.getDeclaredMethods())
                .filter(method -> !method.isSynthetic() && (method.getModifiers() & Modifier.STATIC) == 0)
                .map(Method::getName)
                .sorted()
                .toList();
        assertEquals(List.of("doIt", "getInt", "getLong"), methodNames);
    }

    @Test
    public void testSuperDelegationForInheritedMethods() throws Exception {
        var wrapper = defineWrapper(entryOf(PartialImpl.class));
        var instance = wrapper.getDeclaredConstructor().newInstance();

        assertEquals(7, wrapper.getMethod("getInt").invoke(instance));
        assertEquals(737373L, wrapper.getMethod("getLong").invoke(instance));
        wrapper.getMethod("doIt").invoke(instance);
    }

    @Test
    public void testFailsWhenNoImplementationIsConstructible() throws Exception {
        var wrapper = defineWrapper(entryOf(BadImpl.class));
        var error = assertThrows(InvocationTargetException.class,
                () -> wrapper.getDeclaredConstructor().newInstance());
        var cause = error.getTargetException();
        assertTrue("Expected IllegalStateException, got " + cause, cause instanceof IllegalStateException);
        assertTrue(cause.getMessage(), cause.getMessage().contains("No available implementation"));
        assertEquals(1, cause.getSuppressed().length);
        assertEquals("Incompatible implementation", cause.getSuppressed()[0].getMessage());
    }
}
