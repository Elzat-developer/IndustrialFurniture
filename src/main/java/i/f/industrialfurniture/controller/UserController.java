package i.f.industrialfurniture.controller;

import i.f.industrialfurniture.dto.admin.*;
import i.f.industrialfurniture.dto.order.OrderDetailsDto;
import i.f.industrialfurniture.dto.order.OrderHistoryUserDto;
import i.f.industrialfurniture.dto.order.OrderRequestDto;
import i.f.industrialfurniture.dto.order.OrderResponseDto;
import i.f.industrialfurniture.dto.user.*;
import i.f.industrialfurniture.model.CategoryType;
import i.f.industrialfurniture.model.ProductType;
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
    public ResponseEntity<List<GetCategoriesUserDto>> getCategories(@RequestParam CategoryType categoryType){
        List<GetCategoriesUserDto> categoriesUserDtoList = userService.getCategories(categoryType);
        return ResponseEntity.ok(categoriesUserDtoList);
    }
    @GetMapping("/get_tech_spec")
    public ResponseEntity<List<GetTechSpecDto>> getTechSpecs(){
        List<GetTechSpecDto> techSpecDtoList = adminService.getTechSpecs();
        return ResponseEntity.ok(techSpecDtoList);
    }
    @GetMapping("/get_products")
    public ResponseEntity<List<GetProductsUserDto>> getProductsUserDto(@RequestParam ProductType productType){
        List<GetProductsUserDto> getProductsUserDtoList = userService.getProductsUserDto(productType);
        return ResponseEntity.ok(getProductsUserDtoList);
    }
    @GetMapping("/get_product/{productId}")
    public ResponseEntity<GetProductDto> getProduct(@PathVariable Integer productId){
        GetProductDto getProductDto = adminService.getProduct(productId);
        return ResponseEntity.ok(getProductDto);
    }
    @GetMapping("/get_products_filter")
    public ResponseEntity<List<GetProductsUserDto>> getFilteredProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String material
    ) {
        List<GetProductsUserDto> products = adminService.findProducts(
                active,
                productType,
                categoryId,
                material,
                minPrice,
                maxPrice
        );

        return ResponseEntity.ok(products);
    }
    @PostMapping("/generate_cp_pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestBody List<CartItemDto> cartItemDtoList) {
        // 1. Считаем общую сумму (лучше сделать это здесь или в сервисе перед передачей в PDF)
        BigDecimal totalSum = cartItemDtoList.stream()
                .map(item -> item.productPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Вызываем новый метод генерации PDF
        byte[] fileContent = userService.generateCpPdf(cartItemDtoList, totalSum);

        // 3. Возвращаем PDF ответ
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Commercial_Proposal.pdf")
                .contentType(MediaType.APPLICATION_PDF) // Указываем, что это PDF
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
