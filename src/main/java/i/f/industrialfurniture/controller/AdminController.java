package i.f.industrialfurniture.controller;

import i.f.industrialfurniture.dto.admin.*;
import i.f.industrialfurniture.dto.order.GetOrdersDto;
import i.f.industrialfurniture.dto.user.CreateProductDto;
import i.f.industrialfurniture.model.PaidStatus;
import i.f.industrialfurniture.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
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
    @PostMapping(value = "/create_product",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createProduct(@ModelAttribute CreateProductDto createProductDto){
        adminService.createProduct(createProductDto);
        return ResponseEntity.ok("Product created");
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
    @PutMapping(value = "/edit_product",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editProduct(@ModelAttribute EditProductDto editProduct){
        adminService.editProduct(editProduct);
        return ResponseEntity.ok("Product edited");
    }
    @DeleteMapping("/delete_product/{product_id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer product_id){
        adminService.deleteProduct(product_id);
        return ResponseEntity.ok("Delete Product");
    }
    @GetMapping("/get_categories")
    public ResponseEntity<List<GetCategories>> getCategories(){
        List<GetCategories> categories = adminService.getCategories();
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
