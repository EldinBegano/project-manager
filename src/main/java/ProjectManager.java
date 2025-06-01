import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unused")
public class ProjectManager {
    private static final String CONFIG_FILE = System.getProperty("user.home") + "/.project_manager_config";
    private static final String DEFAULT_WORKSPACE = System.getProperty("user.home") + "/Projects";
    private static final Scanner scanner = new Scanner(System.in);

    // ANSI color
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";

    // Colors
    private static final String BLACK = "\033[30m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String PURPLE = "\033[35m";
    private static final String CYAN = "\033[36m";
    private static final String WHITE = "\033[37m";

    // Background colors
    private static final String BG_BLACK = "\033[40m";
    private static final String BG_RED = "\033[41m";
    private static final String BG_GREEN = "\033[42m";
    private static final String BG_YELLOW = "\033[43m";
    private static final String BG_BLUE = "\033[44m";
    private static final String BG_PURPLE = "\033[45m";
    private static final String BG_CYAN = "\033[46m";
    private static final String BG_WHITE = "\033[47m";

    // Box chars
    private static final String TOP_LEFT = "┌";
    private static final String TOP_RIGHT = "┐";
    private static final String BOTTOM_LEFT = "└";
    private static final String BOTTOM_RIGHT = "┘";
    private static final String HORIZONTAL = "─";
    private static final String VERTICAL = "│";
    private static final String CROSS = "┼";
    private static final String T_DOWN = "┬";
    private static final String T_UP = "┴";
    private static final String T_RIGHT = "├";
    private static final String T_LEFT = "┤";

    private String workspacePath;

    public ProjectManager() {
        loadConfig();
        ensureWorkspaceExists();
    }

    public static void main(String[] args) {
        ProjectManager pm = new ProjectManager();
        pm.run();
    }

    private void run() {
        clearScreen();
        showHeader();

        while (true) {
            showMenu();
            String choice = scanner.nextLine().trim();

            clearScreen();
            showHeader();

            switch (choice) {
                case "1":
                    showAllProjects();
                    break;
                case "2":
                    createNewProject();
                    break;
                case "3":
                    changeWorkspace();
                    break;
                case "4":
                    showGoodbye();
                    return;
                default:
                    showError("Invalid choice. Please try again.");
            }

            if (!choice.equals("2")) {
                System.out.println("\n" + DIM + "Press Enter to continue..." + RESET);
                scanner.nextLine();
                clearScreen();
                showHeader();
            }
        }
    }

    private void clearScreen() {
        System.out.print("\033[2J\033[H");
    }

    private void showHeader() {
        String title = "PROJECT MANAGER";
        String workspace = "Workspace: " + workspacePath;

        System.out.println(BOLD + CYAN + TOP_LEFT + HORIZONTAL.repeat(50) + RESET);
        System.out.println(BOLD + CYAN + VERTICAL + " " + WHITE + BG_BLUE + " " + title + " " + RESET);
        System.out.println(BOLD + CYAN + T_RIGHT + HORIZONTAL.repeat(50) + RESET);
        System.out.println(BOLD + CYAN + VERTICAL + " " + YELLOW + workspace + RESET);
        System.out.println(BOLD + CYAN + BOTTOM_LEFT + HORIZONTAL.repeat(50) + RESET);
        System.out.println();
    }

    private void showMenu() {
        System.out.println(BOLD + WHITE + "┌─ Main Menu " + HORIZONTAL.repeat(20) + RESET);
        System.out.println(BOLD + WHITE + "│" + RESET);
        System.out.println(BOLD + WHITE + "│ " + GREEN + "1" + WHITE + " → " + RESET + "Show all projects");
        System.out.println(BOLD + WHITE + "│ " + GREEN + "2" + WHITE + " → " + RESET + "Create new project");
        System.out.println(BOLD + WHITE + "│ " + GREEN + "3" + WHITE + " → " + RESET + "Change workspace directory");
        System.out.println(BOLD + WHITE + "│ " + RED + "4" + WHITE + " → " + RESET + "Exit");
        System.out.println(BOLD + WHITE + "│" + RESET);
        System.out.println(BOLD + WHITE + "└" + HORIZONTAL.repeat(31) + RESET);
        System.out.print("\n" + BOLD + CYAN + "❯ " + RESET + "Enter your choice: ");
    }

    private void showAllProjects() {
        System.out.println(BOLD + PURPLE + "┌─ Your Projects ─" + HORIZONTAL.repeat(45) + RESET);

        File workspace = new File(workspacePath);
        if (!workspace.exists() || !workspace.isDirectory()) {
            showError("Workspace directory not found: " + workspacePath);
            return;
        }

        File[] projects = workspace.listFiles(File::isDirectory);
        if (projects == null || projects.length == 0) {
            System.out.println(BOLD + PURPLE + "│ " + RESET + DIM + "No projects found in workspace.");
            System.out.println(BOLD + PURPLE + "└" + HORIZONTAL.repeat(55) + RESET);
            return;
        }

        List<ProjectInfo> projectList = new ArrayList<>();

        for (File project : projects) {
            try {
                Path projectPath = project.toPath();
                BasicFileAttributes attrs = Files.readAttributes(projectPath, BasicFileAttributes.class);
                LocalDateTime lastModified = LocalDateTime.ofInstant(
                        attrs.lastModifiedTime().toInstant(),
                        java.time.ZoneId.systemDefault()
                );

                if (isProjectDirectory(project)) {
                    String language = detectProjectLanguage(project);
                    projectList.add(new ProjectInfo(project.getName(), lastModified, language));
                }
            } catch (IOException e) {
                System.err.println("Error reading project: " + project.getName());
            }
        }

        if (projectList.isEmpty()) {
            System.out.println(BOLD + PURPLE + "│ " + RESET + DIM + "No valid projects found in workspace.");
            System.out.println(BOLD + PURPLE + "└" + HORIZONTAL.repeat(55) + RESET);
            return;
        }

        projectList.sort((a, b) -> b.lastModified.compareTo(a.lastModified));

        System.out.println(BOLD + PURPLE + "│ " + WHITE + "Project Name" + " ".repeat(18) + " Last Modified" + " ".repeat(7) + "Language");
        System.out.println(BOLD + PURPLE + T_RIGHT + HORIZONTAL.repeat(30) + T_DOWN + HORIZONTAL.repeat(20) +
                T_DOWN + HORIZONTAL.repeat(10) + RESET);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");

        for (int i = 0; i < projectList.size(); i++) {
            ProjectInfo project = projectList.get(i);
            String langColor = getLanguageColor(project.language);
            String projectName = truncateString(project.name);
            String lastMod = project.lastModified.format(formatter);

            System.out.println(BOLD + PURPLE + "│ " + RESET + (i % 2 == 0 ? WHITE : DIM) + projectName +
                    " ".repeat(31 - projectName.length()) + CYAN + lastMod + " ".repeat(21 - lastMod.length()) +
                    langColor + getProjectIcon(project.language) + " " + project.language);
        }

        System.out.println(BOLD + PURPLE + "└" + HORIZONTAL.repeat(62) + RESET);

        System.out.println("\n" + BOLD + GREEN + "✓ " + RESET + "Found " + BOLD + projectList.size() + RESET + " projects");
    }

    private void createNewProject() {
        System.out.println(BOLD + GREEN + "┌─ Create New Project " + HORIZONTAL.repeat(30) + RESET);
        System.out.println(BOLD + GREEN + "│" + RESET);

        System.out.print(BOLD + GREEN + "│ " + RESET + BOLD + "Project Name: " + RESET);
        String projectName = scanner.nextLine().trim();

        if (projectName.isEmpty()) {
            showError("Project name cannot be empty.");
            return;
        }

        projectName = projectName.replaceAll("[^a-zA-Z0-9_-]", "_");
        System.out.println(BOLD + GREEN + "│ " + RESET + DIM + "Sanitized to: " + CYAN + projectName + RESET);

        System.out.println(BOLD + GREEN + "│" + RESET);
        System.out.println(BOLD + GREEN + "│ " + WHITE + "Select Programming Language:" + RESET);
        System.out.println(BOLD + GREEN + "│" + RESET);
        System.out.println(BOLD + GREEN + "│ " + YELLOW + "1" + WHITE + " → " + RESET + "☕ Java" + " ".repeat(25) + YELLOW + "3" + WHITE + " → " + RESET + "🎯 Dart");
        System.out.println(BOLD + GREEN + "│ " + YELLOW + "2" + WHITE + " → " + RESET + "🟨 JavaScript/Node.js " + " ".repeat(10) + YELLOW + "4" + WHITE + " → " + RESET + "🐹 Go");
        System.out.println(BOLD + GREEN + "│" + RESET);

        System.out.print(BOLD + GREEN + "│ " + RESET + BOLD + "Choice (1-4): " + RESET);
        String langChoice = scanner.nextLine().trim();

        ProjectLanguage language = switch (langChoice) {
            case "1" -> ProjectLanguage.JAVA;
            case "2" -> ProjectLanguage.JAVASCRIPT;
            case "3" -> ProjectLanguage.DART;
            case "4" -> ProjectLanguage.GO;
            default -> {
                System.out.println(BOLD + GREEN + "│ " + RESET + YELLOW + "⚠ Invalid choice. Defaulting to Java." + RESET);
                yield ProjectLanguage.JAVA;
            }
        };

        System.out.println(BOLD + GREEN + "│ " + RESET + "Selected: " + getLanguageColor(language.name()) +
                getProjectIcon(language.name()) + " " + language.name() + RESET);
        System.out.println(BOLD + GREEN + "│" + RESET);

        System.out.print(BOLD + GREEN + "│ " + RESET + BOLD + "Initialize Git repository? (y/n): " + RESET);
        boolean initGit = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.println(BOLD + GREEN + "│ " + RESET + "Git: " + (initGit ? GREEN + "✓ Yes" : RED + "✗ No") + RESET);
        System.out.println(BOLD + GREEN + "│" + RESET);
        System.out.println(BOLD + GREEN + "└" + HORIZONTAL.repeat(50) + RESET);

        System.out.println("\n" + BOLD + YELLOW + "🔨 Creating project..." + RESET);

        try {
            Path projectPath = createProjectStructure(projectName, language, initGit);
            System.out.println(BOLD + GREEN + "✅ Project '" + WHITE + projectName + GREEN + "' created successfully!" + RESET);
            System.out.println(BOLD + CYAN + "🚀 Opening project directory and exiting..." + RESET);

            Thread.sleep(1000);
            changeToProjectDirectory(projectPath);
            System.exit(0);

        } catch (Exception e) {
            showError("Error creating project: " + e.getMessage());
        }
    }

    private void changeWorkspace() {
        System.out.println(BOLD + BLUE + "┌─ Change Workspace " + HORIZONTAL.repeat(30) + RESET);
        System.out.println(BOLD + BLUE + "│" + RESET);
        System.out.println(BOLD + BLUE + "│ " + WHITE + "Current: " + CYAN + workspacePath + RESET);
        System.out.println(BOLD + BLUE + "│" + RESET);

        System.out.print(BOLD + BLUE + "│ " + RESET + BOLD + "New workspace path: " + RESET);
        String newPath = scanner.nextLine().trim();

        if (newPath.isEmpty()) {
            System.out.println(BOLD + BLUE + "│ " + RESET + YELLOW + "⚠ Workspace path unchanged." + RESET);
            System.out.println(BOLD + BLUE + "└" + HORIZONTAL.repeat(48) + RESET);
            return;
        }

        File newWorkspace = new File(newPath);
        if (!newWorkspace.exists()) {
            System.out.print(BOLD + BLUE + "│ " + RESET + "Directory doesn't exist. Create it? (y/n): ");
            if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                try {
                    Files.createDirectories(newWorkspace.toPath());
                    System.out.println(BOLD + BLUE + "│ " + RESET + GREEN + "✓ Directory created." + RESET);
                } catch (IOException e) {
                    showError("Failed to create directory: " + e.getMessage());
                    return;
                }
            } else {
                System.out.println(BOLD + BLUE + "│ " + RESET + RED + "✗ Operation cancelled." + RESET);
                System.out.println(BOLD + BLUE + "└" + HORIZONTAL.repeat(48) + RESET);
                return;
            }
        }

        workspacePath = newPath;
        saveConfig();
        System.out.println(BOLD + BLUE + "│ " + RESET + GREEN + "✓ Workspace changed to: " + CYAN + workspacePath + RESET);
        System.out.println(BOLD + BLUE + "│" + RESET);
        System.out.println(BOLD + BLUE + "└" + HORIZONTAL.repeat(48) + RESET);
    }

    private void showError(String message) {
        System.out.println(BOLD + RED + "┌─ Error " + HORIZONTAL.repeat(40) + RESET);
        System.out.println(BOLD + RED + "│ " + RESET + "❌ " + message);
        System.out.println(BOLD + RED + "└" + HORIZONTAL.repeat(47) + RESET);
    }

    private void showGoodbye() {
        System.out.println(BOLD + PURPLE + "┌─ Goodbye! " + HORIZONTAL.repeat(35) + RESET);
        System.out.println(BOLD + PURPLE + "│" + RESET);
        System.out.println(BOLD + PURPLE + "│ " + RESET + GREEN + "👋 Thank you for using MLP Project Manager!");
        System.out.println(BOLD + PURPLE + "│" + RESET);
        System.out.println(BOLD + PURPLE + "└" + HORIZONTAL.repeat(46) + RESET);
    }

    private String getLanguageColor(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> YELLOW + BOLD;
            case "javascript" -> YELLOW;
            case "dart" -> BLUE + BOLD;
            case "go" -> CYAN + BOLD;
            case "python" -> GREEN + BOLD;
            case "rust" -> RED + BOLD;
            default -> WHITE;
        };
    }

    private String getProjectIcon(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "☕";
            case "javascript" -> "🟨";
            case "dart" -> "🎯";
            case "go" -> "🐹";
            case "python" -> "🐍";
            case "rust" -> "🦀";
            default -> "📁";
        };
    }

    private String truncateString(String str) {
        if (str.length() <= 28) {
            return str;
        }
        return str.substring(0, 28 - 3) + "...";
    }

    private void changeToProjectDirectory(Path projectPath) {
        try {
            String absolutePath = projectPath.toAbsolutePath().toString();

            System.setProperty("user.dir", absolutePath);

            String shell = System.getenv("SHELL");
            if (shell == null) {
                shell = "/bin/bash";
            }

            int exitCode = getExitCode(shell, absolutePath);
            System.exit(exitCode);

        } catch (Exception e) {
            String absolutePath = projectPath.toAbsolutePath().toString();
            System.setProperty("user.dir", absolutePath);
            System.out.println("Changed to project directory: " + absolutePath);
            System.err.println("Note: Terminal directory change may not persist after program exit.");
        }
    }

    private static int getExitCode(String shell, String absolutePath) throws IOException, InterruptedException {
        String[] command = getStrings(shell, absolutePath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();

        return process.waitFor();
    }

    private static String[] getStrings(String shell, String absolutePath) {
        String[] command;
        if (shell.contains("bash")) {
            command = new String[]{"/bin/bash", "-c", "cd '" + absolutePath + "' && exec bash"};
        } else if (shell.contains("zsh")) {
            command = new String[]{shell, "-c", "cd '" + absolutePath + "' && exec zsh"};
        } else if (shell.contains("fish")) {
            command = new String[]{shell, "-c", "cd '" + absolutePath + "'; exec fish"};
        } else {
            command = new String[]{shell, "-c", "cd '" + absolutePath + "' && exec " + shell};
        }
        return command;
    }

    private Path createProjectStructure(String projectName, ProjectLanguage language, boolean initGit) throws IOException {
        Path projectPath = Paths.get(workspacePath, projectName);

        if (Files.exists(projectPath)) {
            System.out.println("Project directory already exists: " + projectPath);
            return projectPath;
        }

        Files.createDirectories(projectPath);

        switch (language) {
            case JAVA:
                createJavaProject(projectPath, projectName);
                break;
            case JAVASCRIPT:
                createJavaScriptProject(projectPath, projectName);
                break;
            case DART:
                createDartProject(projectPath, projectName);
                break;
            case GO:
                createGoProject(projectPath, projectName);
                break;
        }

        createReadme(projectPath, projectName, language);
        createGitignore(projectPath, language);

        if (initGit) {
            initializeGitRepository(projectPath);
        }

        return projectPath;
    }

    private void createJavaProject(Path projectPath, String projectName) throws IOException {
        Files.createDirectories(projectPath.resolve("src/main/java"));
        Files.createDirectories(projectPath.resolve("src/main/resources"));
        Files.createDirectories(projectPath.resolve("src/test/java"));

        String pomContent = String.format(
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <project xmlns="http://maven.apache.org/POM/4.0.0"
                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                            <modelVersion>4.0.0</modelVersion>
                           \s
                            <groupId>com.example</groupId>
                            <artifactId>%s</artifactId>
                            <version>1.0.0</version>
                           \s
                            <properties>
                                <maven.compiler.source>11</maven.compiler.source>
                                <maven.compiler.target>11</maven.compiler.target>
                                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                            </properties>
                           \s
                            <dependencies>
                                <dependency>
                                    <groupId>junit</groupId>
                                    <artifactId>junit</artifactId>
                                    <version>4.13.2</version>
                                    <scope>test</scope>
                                </dependency>
                            </dependencies>
                        </project>""", projectName
        );

        Files.write(projectPath.resolve("pom.xml"), pomContent.getBytes());

        String mainClass = String.format(
                """
                        public class Main {
                            public static void main(String[] args) {
                                System.out.println("Hello, %s!");
                            }
                        }""", projectName
        );

        Files.write(projectPath.resolve("src/main/java/Main.java"), mainClass.getBytes());
    }

    private void createJavaScriptProject(Path projectPath, String projectName) throws IOException {
        String packageJson = String.format(
                """
                        {
                          "name": "%s",
                          "version": "1.0.0",
                          "description": "",
                          "main": "index.js",
                          "scripts": {
                            "start": "node index.js",
                            "test": "echo \\"Error: no test specified\\" && exit 1"
                          },
                          "keywords": [],
                          "author": "",
                          "license": "ISC"
                        }""", projectName
        );

        Files.write(projectPath.resolve("package.json"), packageJson.getBytes());

        String indexJs = String.format(
                "console.log('Hello, %s!');\n", projectName
        );

        Files.write(projectPath.resolve("index.js"), indexJs.getBytes());
        Files.createDirectories(projectPath.resolve("src"));
        Files.createDirectories(projectPath.resolve("test"));
    }

    private void createDartProject(Path projectPath, String projectName) throws IOException {
        String pubspecYaml = String.format(
                """
                        name: %s
                        description: A new Dart project
                        version: 1.0.0
                        
                        environment:
                          sdk: '>=2.17.0 <4.0.0'
                        
                        dev_dependencies:
                          test: ^1.21.0
                        """, projectName
        );

        Files.write(projectPath.resolve("pubspec.yaml"), pubspecYaml.getBytes());

        Files.createDirectories(projectPath.resolve("lib"));
        Files.createDirectories(projectPath.resolve("test"));
        Files.createDirectories(projectPath.resolve("bin"));

        String mainDart = String.format(
                """
                        void main() {
                          print('Hello, %s!');
                        }""", projectName
        );

        Files.write(projectPath.resolve("bin/main.dart"), mainDart.getBytes());
    }

    private void createGoProject(Path projectPath, String projectName) throws IOException {
        String goMod = String.format(
                """
                        module %s
                        
                        go 1.21
                        """, projectName
        );

        Files.write(projectPath.resolve("go.mod"), goMod.getBytes());

        String mainGo = String.format(
                """
                        package main
                        
                        import "fmt"
                        
                        func main() {
                            fmt.Println("Hello, %s!")
                        }""", projectName
        );

        Files.write(projectPath.resolve("main.go"), mainGo.getBytes());

        Files.createDirectories(projectPath.resolve("cmd"));
        Files.createDirectories(projectPath.resolve("pkg"));
        Files.createDirectories(projectPath.resolve("internal"));
    }

    private void createReadme(Path projectPath, String projectName, ProjectLanguage language) throws IOException {
        String readme = String.format(
                """
                        # %s
                        
                        A %s project.
                        
                        ## Getting Started
                        
                        %s
                        
                        ## License
                        
                        This project is licensed under the MIT License.
                        """,
                projectName,
                language.name().toLowerCase(),
                getRunInstructions(language)
        );

        Files.write(projectPath.resolve("README.md"), readme.getBytes());
    }

    private String getRunInstructions(ProjectLanguage language) {
        return switch (language) {
            case JAVA -> "Run with Maven:\n```bash\nmvn compile exec:java -Dexec.mainClass=\"Main\"\n```";
            case JAVASCRIPT -> "Run with Node.js:\n```bash\nnpm start\n```";
            case DART -> "Run with Dart:\n```bash\ndart run bin/main.dart\n```";
            case GO -> "Run with Go:\n```bash\ngo run main.go\n```";
        };
    }

    private void createGitignore(Path projectPath, ProjectLanguage language) throws IOException {
        StringBuilder gitignore = new StringBuilder();

        gitignore.append("# IDE files\n");
        gitignore.append(".vscode/\n");
        gitignore.append(".idea/\n");
        gitignore.append("*.swp\n");
        gitignore.append("*.swo\n");
        gitignore.append(".DS_Store\n\n");

        switch (language) {
            case JAVA:
                gitignore.append("# Maven\n");
                gitignore.append("target/\n");
                gitignore.append("pom.xml.tag\n");
                gitignore.append("pom.xml.releaseBackup\n");
                gitignore.append("pom.xml.versionsBackup\n");
                gitignore.append("pom.xml.next\n\n");
                gitignore.append("# Java\n");
                gitignore.append("*.class\n");
                gitignore.append("*.jar\n");
                gitignore.append("*.war\n");
                gitignore.append("*.ear\n");
                break;
            case JAVASCRIPT:
                gitignore.append("# Node.js\n");
                gitignore.append("node_modules/\n");
                gitignore.append("npm-debug.log*\n");
                gitignore.append("yarn-debug.log*\n");
                gitignore.append("yarn-error.log*\n");
                gitignore.append(".env\n");
                break;
            case DART:
                gitignore.append("# Dart\n");
                gitignore.append(".dart_tool/\n");
                gitignore.append("build/\n");
                gitignore.append("pubspec.lock\n");
                break;
            case GO:
                gitignore.append("# Go\n");
                gitignore.append("*.exe\n");
                gitignore.append("*.exe~\n");
                gitignore.append("*.dll\n");
                gitignore.append("*.so\n");
                gitignore.append("*.dylib\n");
                gitignore.append("*.test\n");
                gitignore.append("*.out\n");
                gitignore.append("go.work\n");
                break;
        }

        Files.write(projectPath.resolve(".gitignore"), gitignore.toString().getBytes());
    }

    private void initializeGitRepository(Path projectPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "init");
            pb.directory(projectPath.toFile());
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println(GREEN + "✓ Git repository initialized." + RESET);

                pb = new ProcessBuilder("git", "add", ".");
                pb.directory(projectPath.toFile());
                process = pb.start();
                process.waitFor();

                pb = new ProcessBuilder("git", "commit", "-m", "Initial commit");
                pb.directory(projectPath.toFile());
                process = pb.start();
                process.waitFor();

                System.out.println(GREEN + "✓ Initial commit created." + RESET);
            } else {
                System.err.println(RED + "✗ Failed to initialize Git repository." + RESET);
            }
        } catch (Exception e) {
            System.err.println(RED + "✗ Error initializing Git repository: " + e.getMessage() + RESET);
        }
    }

    private boolean isProjectDirectory(File dir) {
        String[] projectFiles = {"pom.xml", "package.json", "pubspec.yaml", "go.mod",
                "build.gradle", "Cargo.toml", "requirements.txt"};

        for (String file : projectFiles) {
            if (new File(dir, file).exists()) {
                return true;
            }
        }

        String[] sourceDirs = {"src", "lib", "app"};
        for (String srcDir : sourceDirs) {
            if (new File(dir, srcDir).isDirectory()) {
                return true;
            }
        }

        return false;
    }

    private String detectProjectLanguage(File dir) {
        if (new File(dir, "pom.xml").exists() || new File(dir, "build.gradle").exists()) {
            return "Java";
        }
        if (new File(dir, "package.json").exists()) {
            return "JavaScript";
        }
        if (new File(dir, "pubspec.yaml").exists()) {
            return "Dart";
        }
        if (new File(dir, "go.mod").exists()) {
            return "Go";
        }
        if (new File(dir, "Cargo.toml").exists()) {
            return "Rust";
        }
        if (new File(dir, "requirements.txt").exists() || new File(dir, "pyproject.toml").exists()) {
            return "Python";
        }

        return "Unknown";
    }

    private void loadConfig() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                List<String> lines = Files.readAllLines(configPath);
                if (!lines.isEmpty()) {
                    workspacePath = lines.getFirst();
                } else {
                    workspacePath = DEFAULT_WORKSPACE;
                }
            } else {
                workspacePath = DEFAULT_WORKSPACE;
            }
        } catch (IOException e) {
            workspacePath = DEFAULT_WORKSPACE;
        }
    }

    private void saveConfig() {
        try {
            Files.write(Paths.get(CONFIG_FILE), workspacePath.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    private void ensureWorkspaceExists() {
        try {
            Files.createDirectories(Paths.get(workspacePath));
        } catch (IOException e) {
            System.err.println("Failed to create workspace directory: " + e.getMessage());
        }
    }

}