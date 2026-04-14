package com.moneibakang.informationretrievalbackend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DatasetPathResolver {

    private final String defaultCisiPath;
    private final String defaultPubmedPath;

    public DatasetPathResolver(
            @Value("${existing.file.path:}") String defaultCisiPath,
            @Value("${pubmed.file.path:}") String defaultPubmedPath) {
        this.defaultCisiPath = defaultCisiPath;
        this.defaultPubmedPath = defaultPubmedPath;
    }

    public Path resolveCisiPath(String filePathOrNull) throws FileNotFoundException {
        Set<String> candidates = new LinkedHashSet<>();
        if (filePathOrNull != null && !filePathOrNull.isBlank()) {
            candidates.add(filePathOrNull.trim());
        }
        if (defaultCisiPath != null && !defaultCisiPath.isBlank()) {
            candidates.add(defaultCisiPath.trim());
        }
        candidates.add("uploaded-files/CISI.ALL");
        candidates.add("uploaded-files/cisi.all");
        candidates.add(Paths.get(System.getProperty("user.dir", "."), "uploaded-files", "CISI.ALL").toString());
        candidates.add("src/main/resources/CISI.ALL");

        List<String> tried = new ArrayList<>();
        for (String c : candidates) {
            tried.add(c);
            Path p = Paths.get(c).normalize();
            if (isNonEmptyFile(p)) {
                return p;
            }
        }
        throw new FileNotFoundException(
                "CISI collection file not found. Download CISI.ALL into uploaded-files/ or set existing.file.path. Tried: "
                        + String.join(", ", tried));
    }

    public Path resolvePubmedPath(String filePathOrNull) throws FileNotFoundException {
        Set<String> candidates = new LinkedHashSet<>();
        if (filePathOrNull != null && !filePathOrNull.isBlank()) {
            candidates.add(filePathOrNull.trim());
        }
        if (defaultPubmedPath != null && !defaultPubmedPath.isBlank()) {
            candidates.add(defaultPubmedPath.trim());
        }
        candidates.add("uploaded-files/pubmed_medline.txt");
        candidates.add("uploaded-files/pubmed.xml");
        candidates.add(Paths.get(System.getProperty("user.dir", "."), "uploaded-files", "pubmed_medline.txt").toString());
        candidates.add("src/main/resources/pubmed25n0006.xml");

        List<String> tried = new ArrayList<>();
        for (String c : candidates) {
            tried.add(c);
            Path p = Paths.get(c).normalize();
            if (isNonEmptyFile(p)) {
                return p;
            }
        }
        throw new FileNotFoundException(
                "PubMed corpus file not found. Place a non-empty PubMed MEDLINE (.txt) or PubMed XML file under uploaded-files/ "
                        + "or set pubmed.file.path. (Zero-byte placeholder files are ignored.) Tried: "
                        + String.join(", ", tried));
    }

    /** Resolves paths for indexing; skips missing and zero-length files so empty placeholders are not used. */
    private static boolean isNonEmptyFile(Path p) {
        try {
            return Files.isRegularFile(p) && Files.size(p) > 0;
        } catch (IOException e) {
            return false;
        }
    }
}
