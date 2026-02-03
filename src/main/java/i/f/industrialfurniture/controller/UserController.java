package i.f.industrialfurniture.controller;

import i.f.industrialfurniture.dto.admin.*;
import i.f.industrialfurniture.dto.order.OrderDetailsDto;
import i.f.industrialfurniture.dto.order.OrderHistoryUserDto;
import i.f.industrialfurniture.dto.order.OrderRequestDto;
import i.f.industrialfurniture.dto.order.OrderResponseDto;
import i.f.industrialfurniture.dto.user.*;
import i.f.industrialfurniture.service.AdminService;
import i.f.industrialfurniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AdminService adminService;
    @GetMapping("/get_categories")
    public ResponseEntity<List<GetCategoriesUserDto>> getCategories(){
        List<GetCategoriesUserDto> categoriesUserDtoList = userService.getCategories();
        return ResponseEntity.ok(categoriesUserDtoList);
    }
    @GetMapping("/get_tech_spec")
    public ResponseEntity<List<GetTechSpecDto>> getTechSpecs(){
        List<GetTechSpecDto> techSpecDtoList = adminService.getTechSpecs();
        return ResponseEntity.ok(techSpecDtoList);
    }
    @GetMapping("/get_products")
    public ResponseEntity<List<GetProductsDto>> getProducts(){
        List<GetProductsDto> getProductsList = adminService.getProducts();
        return ResponseEntity.ok(getProductsList);
    }
    @GetMapping("/get_product/{product_id}")
    public ResponseEntity<GetProductDto> getProduct(@PathVariable Integer product_id){
        GetProductDto getProductDto = adminService.getProduct(product_id);
        return ResponseEntity.ok(getProductDto);
    }
    @GetMapping("/get_products_filter")
    public ResponseEntity<List<GetProductsDto>> getFilteredProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String material
    ) {
        List<GetProductsDto> products = adminService.findProducts(
                categoryId,
                material,
                minPrice,
                maxPrice
        );

        return ResponseEntity.ok(products);
    }
    @PostMapping("/generate_cp_exel")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody List<CartItemDto> cartItemDtoList){
        byte[] fileContent = userService.generateExcelPriceList(cartItemDtoList);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cpIF.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileContent);
    }
    @GetMapping("/get_cart")
    public ResponseEntity<CartDto> getCart(@RequestHeader("X-Cart-Token") String cartToken) {
        return ResponseEntity.ok(userService.getCart(cartToken));
    }

    @PostMapping("/add_product_to_cart")
    public ResponseEntity<Void> addProductToCart(@RequestHeader("X-Cart-Token") String cartToken,
                                           @RequestBody AddToCartDto dto) {
        userService.addProductToCart(cartToken, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<Void> updateItemQuantity(
            @RequestHeader("X-Cart-Token") String cartToken,
            @PathVariable Integer itemId,
            @RequestParam Integer quantity) throws AccessDeniedException {
        userService.updateItemQuantity(cartToken, itemId, quantity);
        return ResponseEntity.ok().build();
    }

    // Удалить один товар из корзины
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("X-Cart-Token") String cartToken,
            @PathVariable Integer itemId) {
        userService.removeItem(cartToken, itemId);
        return ResponseEntity.noContent().build();
    }

    // Полностью очистить корзину (например, после заказа)
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestHeader("X-Cart-Token") String cartToken) {
        userService.clearCart(cartToken);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/get_user_phone_orders_history/{phone}")
    public ResponseEntity<List<OrderHistoryUserDto>> getOrderHistory(@PathVariable String phone) {
        List<OrderHistoryUserDto> history = userService.getOrdersByPhone(phone);
        return ResponseEntity.ok(history);
    }
    @GetMapping("/order_details/{orderId}")
    public ResponseEntity<OrderDetailsDto> getOrderDetails(@PathVariable Integer orderId) {
        OrderDetailsDto details = userService.getOrderDetails(orderId);
        return ResponseEntity.ok(details);
    }
    @PostMapping("/create_order")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestHeader("X-Cart-Token") String cartToken,
            @RequestBody OrderRequestDto request) {

        OrderResponseDto response = userService.placeOrder(cartToken, request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/get_promotions")
    public ResponseEntity<List<PromotionDto>> getPromotions(){
        List<PromotionDto> promotionDtoList = userService.getPromotions();
        return ResponseEntity.ok(promotionDtoList);
    }
    @GetMapping("/get_news")
    public ResponseEntity<List<NewsDto>> getNews(){
        List<NewsDto> newsDtoList = userService.getNews();
        return ResponseEntity.ok(newsDtoList);
    }
    @GetMapping("/get_news_user")
    public ResponseEntity<List<UserNewsDto>> getUserNews(){
        List<UserNewsDto> userNewsDtoList = userService.getUserNews();
        return ResponseEntity.ok(userNewsDtoList);
    }
    @GetMapping("/get_news/{news_id}")
    public ResponseEntity<NewsIdDto> getNewsId(@PathVariable Integer news_id){
        NewsIdDto news = userService.getNewsId(news_id);
        return ResponseEntity.ok(news);
    }
    @GetMapping("/get_company")
    public ResponseEntity<CompanyDto> getCompany(){
        CompanyDto companyDto = userService.getCompany();
        return ResponseEntity.ok(companyDto);
    }
}
