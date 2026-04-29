package top.fifthlight.mergetools.merger;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.PrintWriter;
import java.nio.file.Path;

public class ExpectActualMergerWorker extends Worker {
    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, String... args) throws Exception {
        ExpectActualMerger.process(sandboxDir, args);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        new ExpectActualMergerWorker().run(args);
    }
}
