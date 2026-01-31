import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class DotfileSync {

    /* =========================
       CONFIGURATION
       ========================= */

    private static final List<DotfileEntry> DOTFILES = List.of(
        // OS Agnostic
        new DotfileEntry(
            Path.of("git/.gitconfig"),
            Path.of(".gitconfig"),
            EnumSet.of(OS.LINUX, OS.MACOS, OS.WINDOWS)
        ),
        new DotfileEntry(
            Path.of("git/.gitignore_global"),
            Path.of(".gitignore_global"),
            EnumSet.of(OS.LINUX, OS.MACOS, OS.WINDOWS)
        ),
        // UNIX Only
        new DotfileEntry(
            Path.of("nvim"),
            Path.of(".config/nvim"),
            EnumSet.of(OS.LINUX, OS.MACOS)
        ),
        new DotfileEntry(
            Path.of(".zprofile"),
            Path.of(".zprofile"),
            EnumSet.of(OS.LINUX, OS.MACOS)
        ),
        new DotfileEntry(
            Path.of(".zshrc"),
            Path.of(".zshrc"),
            EnumSet.of(OS.LINUX, OS.MACOS)
        ),
        new DotfileEntry(
            Path.of(".tmux.conf"),
            Path.of(".tmux.conf"),
            EnumSet.of(OS.LINUX, OS.MACOS)
        ),
        // WINDOWS ONLY
        new DotfileEntry(
            Path.of("glazewm"),
            Path.of(".glzr/glazewm"),
            EnumSet.of(OS.WINDOWS)
        ),
        new DotfileEntry(
            Path.of("zebar"),
            Path.of(".glzr/zebar"),
            EnumSet.of(OS.WINDOWS)
        ),
        new DotfileEntry(
            Path.of("powershell"),
            Path.of("Documents/PowerShell"),
            EnumSet.of(OS.WINDOWS)
        ),
        new DotfileEntry(
            Path.of("nvim"),
            Path.of("AppData/Local/nvim"),
            EnumSet.of(OS.WINDOWS)
        ),
        new DotfileEntry(
            Path.of("idea/.ideavimrc"),
            Path.of(".ideavimrc"),
            EnumSet.of(OS.WINDOWS)
        )
    );

    /* =========================
       ENTRY POINT
       ========================= */

    public static void main(String[] args) {
        if (args.length != 1) {
            printUsageAndExit();
        }

        Command command = Command.from(args[0]);
        OS currentOS = detectOS();

        Path repoRoot = Paths.get("").toAbsolutePath();
        Path homeRoot = Paths.get(System.getProperty("user.home"));

        System.out.println("Running on OS: " + currentOS);
        System.out.println("Repo root: " + repoRoot);
        System.out.println("Home root: " + homeRoot);
        System.out.println("Command: " + command);
        System.out.println();

        for (DotfileEntry entry : DOTFILES) {
            if (!entry.supportedOS().contains(currentOS)) {
                continue;
            }

            try {
                if (command == Command.WRITE) {
                    copy(
                        repoRoot.resolve(entry.repoRelativePath()),
                        homeRoot.resolve(entry.homeRelativePath())
                    );
                } else {
                    copy(
                        homeRoot.resolve(entry.homeRelativePath()),
                        repoRoot.resolve(entry.repoRelativePath())
                    );
                }
            } catch (IOException e) {
                System.err.println("Failed to process: " + entry);
                e.printStackTrace();
            }
        }
    }

    /* =========================
       CORE LOGIC
       ========================= */

    private static void copy(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            System.out.println("Skipping missing source: " + source);
            return;
        }

        if (Files.isDirectory(source)) {
            copyDirectory(source, target);
        } else {
            copyFile(source, target);
        }
    }

    private static void copyFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(
            source,
            target,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES
        );
        System.out.println("Copied file: " + source + " -> " + target);
    }

    private static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

                Path relative = sourceDir.relativize(dir);
                Path target = targetDir.resolve(relative);
                Files.createDirectories(target);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {

                Path relative = sourceDir.relativize(file);
                Path target = targetDir.resolve(relative);
                Files.copy(
                    file,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
                );
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("Copied directory: " + sourceDir + " -> " + targetDir);
    }

    /* =========================
       SUPPORT TYPES
       ========================= */

    private enum Command {
        WRITE,
        SYNC;

        static Command from(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "write" -> WRITE;
                case "sync" -> SYNC;
                default -> throw new IllegalArgumentException("Unknown command: " + value);
            };
        }
    }

    private enum OS {
        LINUX,
        MACOS,
        WINDOWS
    }

    private static OS detectOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) return OS.WINDOWS;
        if (os.contains("mac")) return OS.MACOS;
        return OS.LINUX;
    }

    private record DotfileEntry(
        Path repoRelativePath,
        Path homeRelativePath,
        EnumSet<OS> supportedOS
    ) {}

    private static void printUsageAndExit() {
        System.err.println("Usage: java DotfileSync <write|sync>");
        System.exit(1);
    }
}
