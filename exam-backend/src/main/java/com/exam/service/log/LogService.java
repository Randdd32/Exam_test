package com.exam.service.log;

import com.exam.core.error.NotFoundException;
import com.exam.core.log.NoLogging;
import com.exam.web.dto.log.LogFileDto;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@NoLogging
@Service
public class LogService {
    private static final Path LOG_DIR = Paths.get("logs").toAbsolutePath().normalize();
    private static final String ACTIVE_LOG_NAME = "application.log";

    public List<LogFileDto> getAvailableLogFiles() throws IOException {
        if (!Files.exists(LOG_DIR)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(LOG_DIR)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("application"))
                    .sorted(Comparator.comparing(this::getLastModifiedSafe).reversed())
                    .map(path ->  new LogFileDto(
                            path.getFileName().toString(),
                            getFileSizeSafe(path),
                            getLastModifiedSafe(path)
                    ))
                    .toList();
        }
    }

    public List<String> getTail(String filename, int linesCount) throws IOException {
        if (linesCount < 1 || linesCount > 5000) {
            throw new IllegalArgumentException("Lines count must be between 1 and 5000");
        }

        Path filePath = resolveAndValidatePath(filename);

        List<String> tail = new ArrayList<>(linesCount);
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setPath(filePath)
                .setCharset(StandardCharsets.UTF_8)
                .get()) {
            String line;
            while ((line = reader.readLine()) != null && tail.size() < linesCount) {
                tail.add(line);
            }
        }
        Collections.reverse(tail);
        return tail;
    }

    public Path getLogFilePath(String filename) {
        return resolveAndValidatePath(filename);
    }

    private Path resolveAndValidatePath(String filename) {
        String safeFilename = (filename == null || filename.isBlank()) ? ACTIVE_LOG_NAME : filename;
        Path resolvedPath = LOG_DIR.resolve(safeFilename).normalize().toAbsolutePath();

        if (!resolvedPath.startsWith(LOG_DIR)) {
            throw new SecurityException("Access denied: attempt to bypass log directory boundaries");
        }
        if (!Files.exists(resolvedPath)) {
            throw new NotFoundException("Log file not found: " + safeFilename);
        }
        return resolvedPath;
    }

    private Instant getLastModifiedSafe(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (Exception e) {
            return Instant.EPOCH;
        }
    }

    private long getFileSizeSafe(Path path) {
        try {
            return Files.size(path);
        } catch (Exception e) {
            return 0;
        }
    }
}
