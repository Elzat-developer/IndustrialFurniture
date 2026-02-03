package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record CreateCompanyDto(
        String name,
         String text,
         String email,
         String phone,
        MultipartFile logoUrl,
         String address,
         String requisites,
        String jobStartAndEndDate
) {
}
