package i.f.industrialfurniture.service;

import i.f.industrialfurniture.dto.order.OrderDetailsDto;
import i.f.industrialfurniture.dto.order.OrderHistoryUserDto;
import i.f.industrialfurniture.dto.order.OrderRequestDto;
import i.f.industrialfurniture.dto.order.OrderResponseDto;
import i.f.industrialfurniture.dto.user.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface UserService {
    byte[] generateExcelPriceList(List<CartItemDto> cartItemDtoList);

    UserDetailsService userDetailsService();

    CartDto getCart(String cartToken);

    void addProductToCart(String cartToken, AddToCartDto dto);

    void updateItemQuantity(String cartToken, Integer itemId, Integer quantity) throws AccessDeniedException;

    void removeItem(String cartToken, Integer itemId);

    void clearCart(String cartToken);

    OrderResponseDto placeOrder(String cartToken, OrderRequestDto request);

    List<OrderHistoryUserDto> getOrdersByPhone(String phone);

    OrderDetailsDto getOrderDetails(Integer orderId);

    List<PromotionDto> getPromotions();

    List<NewsDto> getNews();

    CompanyDto getCompany();

    List<UserNewsDto> getUserNews();

    NewsIdDto getNewsId(Integer newsId);

    List<GetCategoriesUserDto> getCategories();

    List<GetProductsUserDto> getProductsUserDto();
}
