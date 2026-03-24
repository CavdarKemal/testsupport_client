package de.creditreform.crefoteam.cte.tesun.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class RecursiveUnzipper {
    public static void main(String[] args) throws IOException {
        Path sourceZip = Paths.get("input.zip");
        Path targetDir = Paths.get("output");

        Files.createDirectories(targetDir);
        RecursiveUnzipper recursiveUnzipper = new RecursiveUnzipper();
        recursiveUnzipper.unzipRecursive(sourceZip, targetDir);
    }

    public void unzipRecursive(Path zipFile, Path targetDir) throws IOException {
        try (FileSystem zipFs = FileSystems.newFileSystem(zipFile, null)) {
            Path root = zipFs.getPath("/");
            try (Stream<Path> paths = Files.walk(root)) {
                paths.forEach(p -> {
                    try {
                        Path resolved = targetDir.resolve(root.relativize(p).toString());
                        if (Files.isDirectory(p)) {
                            Files.createDirectories(resolved);
                        } else {
                            Files.createDirectories(resolved.getParent());
                            Path copiedPath = Files.copy(p, resolved, StandardCopyOption.REPLACE_EXISTING);
                            // Falls eine ZIP-Datei – rekursiv weiter entpacken
                            if (resolved.toString().toLowerCase().endsWith(".zip")) {
                                Path nestedTarget = resolved.getParent().resolve(resolved.getFileName().toString().replaceFirst("\\.zip$", ""));
                                Files.createDirectories(nestedTarget);
                                unzipRecursive(resolved, nestedTarget);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

}
