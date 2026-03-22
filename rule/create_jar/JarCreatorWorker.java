package top.fifthlight.fabazel.jarcreator;

import org.jspecify.annotations.NonNull;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.PrintWriter;
import java.nio.file.Path;

public class JarCreatorWorker extends Worker {
    @Override
    protected int handleRequest(@NonNull PrintWriter out, @NonNull Path sandboxDir, String... args) throws Exception {
        JarCreator.run(out, sandboxDir, args);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        new JarCreatorWorker().run(args);
    }
}
