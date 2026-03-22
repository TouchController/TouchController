package top.fifthlight.fabazel.mappingmerger.context;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader;
import net.fabricmc.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;

import java.io.IOException;
import java.io.Reader;

public enum MappingFormat {
    TINY_FILE("tiny"),
    TINY_2_FILE("tinyv2"),
    PROGUARD_FILE("proguard"),
    PARCHMENT_JSON("parchment");

    private final String name;

    MappingFormat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void read(Reader reader, MappingVisitor visitor) throws IOException {
        switch (this) {
            case TINY_FILE:
                Tiny1FileReader.read(reader, visitor);
                break;
            case TINY_2_FILE:
                Tiny2FileReader.read(reader, visitor);
                break;
            case PROGUARD_FILE:
                ProGuardFileReader.read(reader, visitor);
                break;
            case PARCHMENT_JSON:
                ParchmentFileReader.read(reader, visitor);
                break;
        }
    }
}
