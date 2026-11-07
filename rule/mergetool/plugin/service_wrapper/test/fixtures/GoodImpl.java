package top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture;

import top.fifthlight.mergetools.api.MergeServiceMethod;

public class GoodImpl implements TestService {
    public static boolean doItCalled = false;

    @MergeServiceMethod
    @Override
    public int getInt() {
        return 42;
    }

    @MergeServiceMethod
    @Override
    public long getLong() {
        return 424242424242L;
    }

    @MergeServiceMethod
    @Override
    public void doIt() {
        doItCalled = true;
    }

    // Unannotated: must not be wrapped.
    public String implementationOnlyMethod() {
        return "implementation";
    }
}
