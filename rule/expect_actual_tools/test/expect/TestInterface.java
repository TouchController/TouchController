package top.fifthlight.mergetools.test;

import top.fifthlight.mergetools.api.ExpectFactory;

public interface TestInterface {
    int age();

    String name();

    @ExpectFactory
    interface Factory {
        TestInterface of(int age, String name);
    }
}
