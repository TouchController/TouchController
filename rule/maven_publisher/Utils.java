package top.fifthlight.fabazel.mavenpublisher;

import org.eclipse.aether.artifact.Artifact;

import java.util.function.Function;

public class Utils {
    @SuppressWarnings("unchecked")
    public static <T extends Exception> void sneakyThrow(Exception exception) throws T {
        throw (T) exception;
    }

    @FunctionalInterface
    public interface ThrowableFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public static <T, R, E extends Exception> Function<T, R> unchecked(final ThrowableFunction<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                sneakyThrow(e);
                throw new AssertionError("Unreachable code");
            }
        };
    }

    public static String artifactFileName(Artifact artifact) {
        var builder = new StringBuilder(artifact.getArtifactId()).append('-').append(artifact.getVersion());
        if (!artifact.getClassifier().isEmpty()) {
            builder.append('-').append(artifact.getClassifier());
        }
        builder.append('.').append(artifact.getExtension());
        return builder.toString();
    }
}
