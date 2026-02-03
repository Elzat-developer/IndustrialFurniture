package i.f.industrialfurniture.dto.user;

public record CompanyDto(
        Integer companyId,
        String name,
        String text,
        String email,
        String phone,
        String logoUrl,
        String address,
        String requisites,
        String jobStartAndEndDate
) {
}
