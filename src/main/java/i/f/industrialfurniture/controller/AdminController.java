package i.f.industrialfurniture.controller;

import i.f.industrialfurniture.dto.admin.*;
import i.f.industrialfurniture.dto.order.GetOrdersDto;
import i.f.industrialfurniture.dto.user.CreateProductDto;
import i.f.industrialfurniture.model.CategoryType;
import i.f.industrialfurniture.model.PaidStatus;
import i.f.industrialfurniture.model.ProductType;
import i.f.industrialfurniture.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    @GetMapping("/get_tech_spec")
    public ResponseEntity<List<GetTechSpecDto>> getTechSpecs(){
        List<GetTechSpecDto> techSpecDtoList = adminService.getTechSpecs();
        return ResponseEntity.ok(techSpecDtoList);
    }
    @PutMapping(value = "/edit_company",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editCompany(@ModelAttribute CreateCompanyDto editCompany){
        adminService.editCompany(editCompany);
        return ResponseEntity.ok("Company edited");
    }
    @PostMapping(value = "/create_news",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createNews(@ModelAttribute CreateNewsDto newsDto){
        adminService.createNews(newsDto);
        return ResponseEntity.ok("News created");
    }
    @PutMapping(value = "/edit_news/{news_id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editNews(@PathVariable Integer news_id,@ModelAttribute CreateNewsDto editNews){
        adminService.editNews(news_id,editNews);
        return ResponseEntity.ok("News edited");
    }
    @DeleteMapping("/delete_news/{news_id}")
    public ResponseEntity<String> deleteNews(@PathVariable Integer news_id){
        adminService.deleteNews(news_id);
        return ResponseEntity.ok("Delete News");
    }
    @PostMapping(value = "/create_promotion",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPromotion(@ModelAttribute MultipartFile urlPhoto){
        adminService.createPromotion(urlPhoto);
        return ResponseEntity.ok("Promotion Created");
    }
    @PatchMapping(value = "/edit_promotion_photo/{promotion_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editPromotionPhoto(@PathVariable Integer promotion_id,@ModelAttribute MultipartFile urlPhoto){
        adminService.editPromotion(promotion_id,urlPhoto);
        return ResponseEntity.ok("Promotion Edited");
    }
    @DeleteMapping("/delete_promotion/{promotion_id}")
    public ResponseEntity<String> deletePromotion(@PathVariable Integer promotion_id){
        adminService.deletePromotion(promotion_id);
        return ResponseEntity.ok("Delete Promotion");
    }
    @GetMapping("/get_orders")
    public ResponseEntity<List<GetOrdersDto>> getOrders(){
        List<GetOrdersDto> ordersDtoList = adminService.getOrders();
        return ResponseEntity.ok(ordersDtoList);
    }
    @PatchMapping("/order/{order_id}")
    public ResponseEntity<String> editPaidStatusOrder(@PathVariable Integer order_id,@RequestParam PaidStatus paidStatus){
        adminService.editPaidStatusOrder(order_id,paidStatus);
        return ResponseEntity.ok("Order edited");
    }
    @DeleteMapping("/delete_order/{order_id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer order_id){
        adminService.deleteOrder(order_id);
        return ResponseEntity.ok("Delete Order");
    }
    @PostMapping(value = "/import_zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportReportDto> importProductsFromZip(@RequestParam("file") MultipartFile file) {
        // 1. Проверка: пришел ли файл
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ImportReportDto(0, 1, List.of("Архив не выбран или пуст"))
            );
        }

        // 2. Проверка расширения: теперь ждем именно .zip
        String fileName = file.getOriginalFilename();
        if (!fileName.toLowerCase().endsWith(".zip")) {
            return ResponseEntity.badRequest().body(
                    new ImportReportDto(0, 1, List.of("Неверный формат файла. Ожидается .zip архив с Excel и папкой images"))
            );
        }

        // 3. Вызываем новый метод сервиса
        log.info("📥 Начало импорта ZIP-архива: {}", fileName);
        ImportReportDto importReportDto = adminService.importProductsFromZip(file);

        return ResponseEntity.ok(importReportDto);
    }
    @GetMapping("/get_products")
    public ResponseEntity<List<GetProductsDto>> getProducts(
            @RequestParam ProductType productType,
            @RequestParam Boolean active){
        List<GetProductsDto> getProductsList = adminService.getProducts(productType,active);
        return ResponseEntity.ok(getProductsList);
    }
    @GetMapping("/get_products_filter_admin")
    public ResponseEntity<List<GetProductsDto>> getFilteredProductsAdmin(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String material
    ) {
        List<GetProductsDto> products = adminService.findProductsAdmin(
                active,
                productType,
                categoryId,
                material,
                minPrice,
                maxPrice
        );

        return ResponseEntity.ok(products);
    }
    @PostMapping(value = "/create_product",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createProduct(
            @RequestPart("product") CreateProductDto createProductDto,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ){
        adminService.createProduct(createProductDto,photos);
        return ResponseEntity.ok("Product created");
    }
    @PutMapping(value = "/edit_product",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editProduct(
            @RequestPart("product") EditProductDto editProduct,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ){
        adminService.editProduct(editProduct,photos);
        return ResponseEntity.ok("Product edited");
    }
    @PatchMapping("/edit_product_active/{productId}")
    public ResponseEntity<String> editProductActive(@PathVariable Integer productId){
        adminService.editProductActive(productId);
        return ResponseEntity.ok("Product is Active");
    }
    @DeleteMapping("/delete_product/{product_id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer product_id){
        adminService.deleteProduct(product_id);
        return ResponseEntity.ok("Delete Product");
    }
    @GetMapping("/get_categories")
    public ResponseEntity<List<GetCategories>> getCategories(
            @RequestParam CategoryType categoryType,
            @RequestParam Boolean active
    ){
        List<GetCategories> categories = adminService.getCategories(categoryType,active);
        return ResponseEntity.ok(categories);
    }
    @PostMapping(value = "/create_category",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createCategory(@ModelAttribute CreateCategoryDto createCategoryDto){
        adminService.createCategory(createCategoryDto);
        return ResponseEntity.ok("Category created");
    }
    @PutMapping(value = "/edit_category",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editCategory(@ModelAttribute EditCategoryDto editCategory){
        adminService.editCategory(editCategory);
        return ResponseEntity.ok("Edit Category");
    }
    @PatchMapping("/edit_category_active/{categoryId}")
    public ResponseEntity<String> editCategoryActive(@PathVariable Integer categoryId){
        adminService.editCategoryActive(categoryId);
        return ResponseEntity.ok("Category is Active");
    }
    @DeleteMapping("/delete_category/{category_id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer category_id){
        adminService.deleteCategory(category_id);
        return ResponseEntity.ok("Delete Category");
    }
    @PostMapping(value = "/create_tech_spec",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createTechSpec(@ModelAttribute CreateTechSpec createTechSpec){
        adminService.createTechSpec(createTechSpec);
        return ResponseEntity.ok("TechSpec created");
    }
    @PutMapping(value = "/edit_tech_spec/{tech_spec_id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editTechSpec(@PathVariable Integer tech_spec_id,@ModelAttribute EditTechSpec techSpecDto){
        adminService.editTechSpec(tech_spec_id,techSpecDto);
        return ResponseEntity.ok("TechSpec edited");
    }
    @DeleteMapping("/delete_tech_spec/{tech_spec_id}")
    public ResponseEntity<String> deleteTechSpec(@PathVariable Integer tech_spec_id){
        adminService.deleteTechSpec(tech_spec_id);
        return ResponseEntity.ok("Tech Spec delete");
    }
}
