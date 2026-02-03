package i.f.industrialfurniture.service;

import i.f.industrialfurniture.dto.order.GetOrdersDto;
import i.f.industrialfurniture.dto.user.CreateProductDto;
import i.f.industrialfurniture.dto.admin.*;
import i.f.industrialfurniture.model.PaidStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {
    void createProduct(CreateProductDto createProductDto);

    List<GetProductsDto> getProducts();

    GetProductDto getProduct(Integer productId);

    void editProduct(EditProductDto editProduct);

    void deleteProduct(Integer productId);

    void createCategory(CreateCategoryDto createCategoryDto);

    List<GetCategories> getCategories();

    void editCategory(EditCategoryDto editCategory);

    void deleteCategory(Integer categoryId);

    List<GetProductsDto> findProducts(Integer categoryId, String material, BigDecimal minPrice, BigDecimal maxPrice);

    void createTechSpec(CreateTechSpec createTechSpec);

    List<GetTechSpecDto> getTechSpecs();

    void editTechSpec(Integer tech_spec_id,EditTechSpec techSpecDto);

    void deleteTechSpec(Integer techSpecId);

    List<GetOrdersDto> getOrders();

    void editPaidStatusOrder(Integer orderId, PaidStatus paidStatus);

    void deleteOrder(Integer orderId);

    void createPromotion(MultipartFile urlPhoto);

    void editPromotion(Integer promotionId, MultipartFile urlPhoto);

    void deletePromotion(Integer promotionId);

    void createNews(CreateNewsDto newsDto);

    void editNews(Integer newsId, CreateNewsDto editNews);

    void deleteNews(Integer newsId);

    void editCompany(CreateCompanyDto editCompany);

    ImportReportDto importProductsFromZip(MultipartFile file);
}
