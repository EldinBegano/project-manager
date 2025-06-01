import java.time.LocalDateTime;

public class ProjectInfo {
    String name;
    LocalDateTime lastModified;
    String language;

    ProjectInfo(String name, LocalDateTime lastModified, String language) {
        this.name = name;
        this.lastModified = lastModified;
        this.language = language;
    }
}