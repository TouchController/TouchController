package top.fifthlight.fabazel.remapper;

import net.fabricmc.tinyremapper.api.TrLogger;

import java.io.PrintWriter;

public class PrintLogger implements TrLogger {
    private final PrintWriter printStream;

    public PrintLogger(PrintWriter printStream) {
        this.printStream = printStream;
    }

    @Override
    public void log(TrLogger.Level level, String string) {
        printStream.print('[');
        printStream.print(level.name());
        printStream.print("] ");
        printStream.println(string);
    }
}
