import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PinGenerator {
    private static final String[] URLS = new String[]{ /*INJECT HERE*/};
    private static final String PIN_TARGET = "$PIN_TARGET";

    public static void main(String[] args) throws Exception {
        var outputPath = Path.of(PIN_TARGET).toAbsolutePath();
        try (var client = HttpClient.newHttpClient();
             var output = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            var semaphore = new Semaphore(4);
            var futures = Arrays.stream(URLS)
                    .filter(line -> !line.isEmpty())
                    .map(url -> {
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return downloadAndComputeHash(client, url)
                                .thenApply(hash -> {
                                    semaphore.release();
                                    return new HashEntry(url, hash);
                                });
                    })
                    .toList();
            var entries = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            // Sort the entries by URL to get deterministic output
                            .sorted(Comparator.comparing(HashEntry::url))
                            .collect(Collectors.toList()))
                    .join();
            for (var entry : entries) {
                output.write(entry.url());
                output.write(" ");
                output.write(entry.hash());
                output.newLine();
            }
        }
        System.out.println("Pin generated successfully: " + outputPath);
    }

    private record HashEntry(String url, String hash) {
    }

    private record HasherBodyHandler(String algorithm) implements HttpResponse.BodyHandler<byte[]> {

        @Override
        public HttpResponse.BodySubscriber<byte[]> apply(HttpResponse.ResponseInfo responseInfo) {
            return new HasherBodySubscriber(algorithm);
        }

        private static class HasherBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {

            private MessageDigest digest;
            private final CompletableFuture<byte[]> future = new CompletableFuture<>();
            private Flow.Subscription subscription;

            public HasherBodySubscriber(String algorithm) {
                try {
                    this.digest = MessageDigest.getInstance(algorithm);
                } catch (NoSuchAlgorithmException e) {
                    var exception = new IllegalArgumentException("Unsupported hashing algorithm: " + algorithm, e);
                    future.completeExceptionally(exception);
                }
            }

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(List<ByteBuffer> item) {
                if (digest != null) {
                    for (var buffer : item) {
                        digest.update(buffer);
                    }
                }
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                var hash = digest.digest();
                future.complete(hash);
            }

            @Override
            public CompletionStage<byte[]> getBody() {
                return future;
            }
        }
    }

    private static <T> CompletableFuture<T> retry(Supplier<CompletableFuture<T>> supplier, int attempts) {
        var cf = supplier.get();
        for (int i = 0; i < attempts; i++) {
            cf = cf.exceptionally(t -> supplier.get().join());
        }
        return cf;
    }

    private static CompletableFuture<String> downloadAndComputeHash(HttpClient client, String url) {
        var sha256Url = url + ".sha256";
        var request = HttpRequest.newBuilder(URI.create(sha256Url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        var content = response.body().trim();
                        var parts = content.split("\\s+");
                        return CompletableFuture.completedFuture(parts[0]);
                    } else {
                        return downloadFileAndComputeHash(client, url);
                    }
                })
                .exceptionally(throwable -> downloadFileAndComputeHash(client, url).join());
    }

    private static CompletableFuture<String> downloadFileAndComputeHash(HttpClient client, String url) {
        var request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();
        return retry(() ->
                client.sendAsync(request, new HasherBodyHandler("SHA-256"))
                        .thenApply(response -> {
                            if (response.statusCode() != 200) {
                                throw new RuntimeException("Failed to download " + url + ", status code: " + response.statusCode());
                            }
                            var hash = response.body();
                            var hexString = new StringBuilder();
                            for (var b : hash) {
                                hexString.append(String.format("%02x", b));
                            }
                            return hexString.toString();
                        }), 3);
    }
}
