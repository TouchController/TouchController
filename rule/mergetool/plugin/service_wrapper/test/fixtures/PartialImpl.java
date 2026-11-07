package top.fifthlight.mergetools.merger.plugin.servicewrapper.fixture;

import top.fifthlight.mergetools.api.MergeServiceMethod;

/// Inherited methods are exposed with explicit super delegation.
public class PartialImpl extends PartialBase {
    public PartialImpl() {
    }

    @MergeServiceMethod
    @Override
    public int getInt() {
        return super.getInt();
    }

    @MergeServiceMethod
    @Override
    public long getLong() {
        return super.getLong();
    }

    @MergeServiceMethod
    @Override
    public void doIt() {
    }
}
