package i.f.industrialfurniture.model.entity;

import i.f.industrialfurniture.model.ImportStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "import_histories")
public class ImportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "success_count")
    private Integer successCount;
    @Column(name = "error_count")
    private Integer errorCount;
    @Column(name = "import_status")
    @Enumerated(EnumType.STRING)
    private ImportStatus importStatus;
    @Column(name = "errors_log")
    private String errorsLog;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
