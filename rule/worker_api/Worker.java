package top.fifthlight.bazel.worker.api;

import com.google.devtools.build.lib.worker.ProtoWorkerMessageProcessor;
import com.google.devtools.build.lib.worker.WorkRequestHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class Worker {
    private static class WorkRequestCallbackWrapper extends WorkRequestHandler.WorkRequestCallback {
        public WorkRequestCallbackWrapper(Worker worker) {
            super((request, out) -> {
                try {
                    var sandboxDir = request.getSandboxDir();
                    if (!sandboxDir.isEmpty()) {
                        return worker.handleRequest(
                            out, Path.of(request.getSandboxDir()), request.getArgumentsList().toArray(new String[0]));
                    } else {
                        return worker.handleRequest(
                            out, Path.of("."), request.getArgumentsList().toArray(new String[0]));
                    }
                } catch (Exception e) {
                    e.printStackTrace(out);
                    return 1;
                }
            });
        }
    }

    private static String[] expandArgFiles(@Nullable Path sandboxDir, String[] args) throws IOException {
        var result = new ArrayList<String>(args.length);
        for (var arg : args) {
            if (!arg.startsWith("@")) {
                result.add(arg);
                continue;
            }
            var path = Path.of(arg.substring(1));
            if (sandboxDir != null) {
                path = sandboxDir.resolve(path);
            }
            try (var stream = Files.lines(path)) {
                stream.filter(line -> !line.isEmpty()).forEachOrdered(result::add);
            }
        }
        return result.toArray(new String[0]);
    }

    public final void run(String... args) throws Exception {
        var argsList = new ArrayList<>(Arrays.asList(args));
        var index = argsList.indexOf("--persistent_worker");
        if (index == -1) {
            var out = new PrintWriter(System.out);
            var expandedArgs = expandArgFiles(null, args);
            var status = handleRequest(out, null, expandedArgs);
            out.flush();
            System.exit(status);
        }
        var handlerBuilder = new WorkRequestHandler.WorkRequestHandlerBuilder(
            new WorkRequestCallbackWrapper(this), System.err, new ProtoWorkerMessageProcessor(System.in, System.out));
        handlerBuilder.setCpuUsageBeforeGc(Duration.ofSeconds(10));
        handlerBuilder.setIdleTimeBeforeGc(Duration.ofSeconds(30));
        try (var handler = handlerBuilder.build()) {
            handler.processRequests();
        }
    }

    protected abstract int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, @NonNull String... args)
        throws Exception;
}
