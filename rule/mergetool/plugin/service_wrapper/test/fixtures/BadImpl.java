package top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture;

import top.fifthlight.mergetools.api.MergeServiceMethod;

/// Fails in its constructor, like an implementation compiled against an incompatible game version.
public class BadImpl implements TestService {
    public BadImpl() {
        throw new IllegalStateException("Incompatible implementation");
    }

    @MergeServiceMethod
    @Override
    public int getInt() {
        throw new AssertionError();
    }

    @MergeServiceMethod
    @Override
    public long getLong() {
        throw new AssertionError();
    }

    @MergeServiceMethod
    @Override
    public void doIt() {
        throw new AssertionError();
    }
}
