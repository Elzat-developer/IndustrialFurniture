package i.f.industrialfurniture.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    private String name;
    private String text;
    private String email;
    private String phone;
    @Column(name = "logo_url")
    private String logoUrl;
    private String address;
    private String requisites;
    @Column(name = "job_start_and_end_date")
    private String jobStartAndEndDate;
}
