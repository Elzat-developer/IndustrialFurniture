package i.f.industrialfurniture.service.impl;

import i.f.industrialfurniture.dto.order.*;
import i.f.industrialfurniture.dto.user.*;
import i.f.industrialfurniture.model.PaidStatus;
import i.f.industrialfurniture.model.entity.*;
import i.f.industrialfurniture.repositories.*;
import i.f.industrialfurniture.service.ProductPhotoService;
import i.f.industrialfurniture.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String MANAGER_PHONE = "77472164664";
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PromotionRepo promotionRepo;
    private final NewsRepo newsRepo;
    private final CompanyRepo companyRepo;
    private final CategoryRepo categoryRepo;
    private final ProductPhotoService productPhotoService;
    @Override
    public byte[] generateExcelPriceList(List<CartItemDto> items) {
        if (items == null || items.isEmpty()) {
            return new byte[0];
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Коммерческое предложение");

            // 1. Стили
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Стиль для денежных ячеек (чтобы в Excel они выглядели как валюта)
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            // 2. Шапка
            String[] columns = {"Название товара", "Цена за ед.", "Кол-во", "Итого"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Данные
            int rowIdx = 1;
            BigDecimal totalSum = BigDecimal.ZERO; // Инициализируем нулем для BigDecimal

            for (CartItemDto item : items) {
                Row row = sheet.createRow(rowIdx++);

                // РАСЧЕТ: цена * количество
                BigDecimal itemTotal = item.productPrice().multiply(BigDecimal.valueOf(item.quantity()));
                totalSum = totalSum.add(itemTotal); // Прибавляем к общей сумме

                row.createCell(0).setCellValue(item.productName());

                // Записываем цену и применяем стиль валюты
                Cell priceCell = row.createCell(1);
                priceCell.setCellValue(item.productPrice().doubleValue()); // Excel принимает double
                priceCell.setCellStyle(currencyStyle);

                row.createCell(2).setCellValue(item.quantity());

                // Записываем итог по строке
                Cell itemTotalCell = row.createCell(3);
                itemTotalCell.setCellValue(itemTotal.doubleValue());
                itemTotalCell.setCellStyle(currencyStyle);
            }

            // 4. ИТОГО
            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellValue("ИТОГО к оплате:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalValueCell = totalRow.createCell(3);
            totalValueCell.setCellValue(totalSum.doubleValue());
            totalValueCell.setCellStyle(headerStyle); // Можно добавить и currencyStyle сюда через объединение стилей

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    @Override
    @Transactional
    public CartDto getCart(String cartToken) {
        Cart cart = cartRepo.findByCartToken(cartToken)
                .orElseGet(() -> createNewCart(cartToken));
        List<CartItemDto> dtos = cart.getItems().stream()
                .map(item -> new CartItemDto(
                        item.getId() == null ? null : item.getId(),
                        item.getProduct().getId() == null ? null : item.getProduct().getId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getTag(),
                        item.getProduct().isActive()
                )).toList();

        // Считаем общую сумму через BigDecimal
        BigDecimal total = dtos.stream()
                .map(item -> item.productPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(cart.getId(), dtos, total);
    }

    @Override
    @Transactional
    public void addProductToCart(String cartToken, AddToCartDto dto) {
        Cart cart = cartRepo.findByCartToken(cartToken)
                .orElseGet(() -> createNewCart(cartToken));

        Product product = productRepo.findById(dto.productId())
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        CartItem existingItem = cartItemRepo.findByCartAndProduct(cart, product)
                .orElseGet(() -> new CartItem(cart, product, 0));

        existingItem.setQuantity(existingItem.getQuantity() + dto.quantity());
        cartItemRepo.save(existingItem);
    }

    @Override
    @Transactional
    public void updateItemQuantity(String cartToken, Integer itemId, Integer quantity) throws AccessDeniedException {
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Позиция не найдена"));

        // Проверка безопасности: токен корзины совпадает?
        if (!item.getCart().getCartToken().equals(cartToken)) {
            throw new AccessDeniedException("Это не ваша корзина");
        }

        if (quantity <= 0) {
            cartItemRepo.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepo.save(item);
        }
    }

    @Override
    @Transactional
    public void removeItem(String cartToken, Integer itemId) {
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Позиция не найдена"));

        if (item.getCart().getCartToken().equals(cartToken)) {
            cartItemRepo.delete(item);
        }
    }

    @Override
    @Transactional
    public void clearCart(String cartToken) {
        cartRepo.findByCartToken(cartToken).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepo.save(cart);
        });
    }

    @Override
    @Transactional
    public OrderResponseDto placeOrder(String cartToken, OrderRequestDto customerInfo) {
        // 1. Получаем корзину (через твой уже готовый сервис)
        CartDto cart = getCart(cartToken);
        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста, нельзя оформить заказ");
        }

        // 2. Создаем и сохраняем основной заказ
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(customerInfo.name());
        order.setCustomerPhone(customerInfo.phone());
        order.setTotalPrice(cart.totalPrice());
        order.setOrderStartDate(LocalDateTime.now());
        order.setPaidStatus(PaidStatus.NOTPAY);

        Order savedOrder = orderRepo.save(order);

        // 3. Сохраняем все товары из корзины как позиции заказа (фиксируем цену!)
        List<OrderItem> orderItems = cart.items().stream().map(item -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setProductId(item.productId());
            oi.setProductName(item.productName());
            oi.setTag(item.tag());
            oi.setQuantity(item.quantity());
            oi.setPriceAtPurchase(item.productPrice());
            oi.setProductActive(item.productActive());
            return oi;
        }).toList();

        orderItemRepo.saveAll(orderItems);

        // 4. Генерируем ссылку для WhatsApp
        String waLink = generateWhatsAppLink(savedOrder, orderItems);

        // 5. Очищаем корзину (чтобы после заказа она была пустой)
        clearCart(cartToken);

        return new OrderResponseDto(
                savedOrder.getOrderNumber(),
                savedOrder.getTotalPrice(),
                waLink
        );
    }

    @Override
    public List<OrderHistoryUserDto> getOrdersByPhone(String phone) {
        List<Order> orders = orderRepo.findAllByCustomerPhoneOrderByOrderStartDateDesc(phone);

        return orders.stream().map(order -> {
            // Здесь используй свой маппер или создавай DTO вручную
            return new OrderHistoryUserDto(
                    order.getId() == null ? null : order.getId(),
                    order.getOrderNumber(),
                    order.getTotalPrice(),
                    order.getPaidStatus(), // "PENDING", "COMPLETED" и т.д.
                    order.getOrderStartDate(),
                    generateWhatsAppLink(order, order.getItems()) // Ссылка, если он захочет написать снова
            );
        }).toList();
    }

    @Override
    public OrderDetailsDto getOrderDetails(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден!"));

        // 1. Собираем список всех ID продуктов в этом заказе
        List<Integer> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2. Получаем мапу (ID продукта -> Объект фото или URL)
        // Предположим, метод findPhotosByProductIds возвращает Map<Integer, GetPhotoDto>
        Map<Integer, GetPhotoDto> photoMap = productPhotoService.getPhotosForProducts(productIds);

        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> {
                    // Достаем фото из мапы по ID продукта, если нет — ставим заглушку или null
                    GetPhotoDto photo = photoMap.getOrDefault(item.getProductId(),
                            new GetPhotoDto(0, "default_url"));

                    return new OrderItemDto(
                            item.getQuantity(),
                            new OrderItemProductDTOS(
                                    item.getProductId(),
                                    item.getProductName(),
                                    item.getPriceAtPurchase(),
                                    photo, // Теперь здесь реальное фото
                                    item.getProductActive()
                            )
                    );
                }).toList();

        return new OrderDetailsDto(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getPaidStatus(),
                order.getOrderStartDate(),
                "https://wa.me/77472164664?text=Хочу сделать повторный заказ №" + order.getOrderNumber(),
                items
        );
    }

    @Override
    public List<PromotionDto> getPromotions() {
        List<Promotion> promotions = promotionRepo.findAll();
        return promotions.stream()
                .map(this::toPromotion)
                .toList();
    }

    @Override
    public List<NewsDto> getNews() {
        List<News> news = newsRepo.findAll();
        return news.stream()
                .map(this::toNews)
                .toList();
    }

    @Override
    public CompanyDto getCompany() {
        Company company = companyRepo.findById(1)
                .orElseThrow(() -> new IllegalArgumentException("Company Not Found"));
        return new CompanyDto(
                company.getId() == null ? null : company.getId(),
                company.getName(),
                company.getText(),
                company.getEmail(),
                company.getPhone(),
                company.getLogoUrl(),
                company.getAddress(),
                company.getRequisites(),
                company.getJobStartAndEndDate()
        );
    }

    @Override
    public List<UserNewsDto> getUserNews() {
        List<News> news = newsRepo.findAll();
        return news.stream()
                .map(this::toNewsUser)
                .toList();
    }

    private UserNewsDto toNewsUser(News news) {
        return new UserNewsDto(
                news.getId(),
                news.getName(),
                news.getNewsPhotoUrl(),
                news.getCreateDateNews()
        );
    }

    @Override
    public NewsIdDto getNewsId(Integer newsId) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("News Not Found"));
        return new NewsIdDto(
                news.getId(),
                news.getName(),
                news.getDescription(),
                news.getNewsPhotoUrl(),
                news.getCreateDateNews()
        );
    }

    @Override
    public List<GetCategoriesUserDto> getCategories() {
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
                .map(this::toCategory)
                .toList();
    }

    @Override
    public List<GetProductsUserDto> getProductsUserDto() {
        List<Product> products = productRepo.findAll();
        return products.stream()
                .map(this::toProduct)
                .toList();
    }

    private GetProductsUserDto toProduct(Product product) {
        GetPhotoDto photoDto = null;

        // Проверяем есть ли у продукта фото
        if (product.getPhotos() != null && !product.getPhotos().isEmpty()) {
            ProductImage firstPhoto = product.getPhotos().get(0);
            photoDto = new GetPhotoDto(
                    firstPhoto.getId() == null ? null : firstPhoto.getId(),
                    firstPhoto.getUrl()
            );
        }
        return new GetProductsUserDto(
                product.getId(),
                product.getProductName(),
                product.getPrice(),
                photoDto
        );
    }

    private GetCategoriesUserDto toCategory(Category category) {
        return new GetCategoriesUserDto(
                category.getId(),
                category.getCategoryName(),
                category.getPhotoUrl()
        );
    }

    private NewsDto toNews(News news) {
        return new NewsDto(
                news.getId() == null ? null : news.getId(),
                news.getName(),
                news.getDescription(),
                news.getNewsPhotoUrl()
        );
    }

    private PromotionDto toPromotion(Promotion promotion) {
        return new PromotionDto(
                promotion.getId() == null ? null : promotion.getId(),
                promotion.getUrlPhoto()
        );
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = new Random().nextInt(900) + 100; // 3 случайные цифры
        return "ORD-" + datePart + "-" + randomPart;
    }

    private String generateWhatsAppLink(Order order, List<OrderItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("👋 *НОВЫЙ ЗАКАЗ С САЙТА*\n\n");
        sb.append("📦 *Номер:* ").append(order.getOrderNumber()).append("\n");
        sb.append("👤 *Клиент:* ").append(order.getCustomerName()).append("\n");
        sb.append("📞 *Тел:* ").append(order.getCustomerPhone()).append("\n");
        sb.append("--------------------------\n");
        sb.append("🛒 *СПИСОК ТОВАРОВ:* \n\n");

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            sb.append(i + 1).append(". ")
                    .append(item.getProductName())
                    .append("\n   🧩 *Арт:* ").append(item.getTag()) // Добавили артикул согласно ТЗ
                    .append("\n   🔢 *Кол-во:* ").append(item.getQuantity()).append(" шт.")
                    .append("\n   💵 *Цена:* ").append(item.getPriceAtPurchase()).append(" KZT\n\n");
        }

        sb.append("--------------------------\n");
        sb.append("💰 *ИТОГО К ОПЛАТЕ:* *").append(order.getTotalPrice()).append(" KZT*");

        // Кодируем для URL
        String encodedMessage = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
        return "https://wa.me/" + MANAGER_PHONE + "?text=" + encodedMessage;
    }

    private Cart createNewCart(String cartToken) {
        Cart cart = new Cart();
        cart.setCartToken(cartToken);
        return cartRepo.save(cart);
    }


    @Override
    @Bean
    public UserDetailsService userDetailsService(){
        return userRepo::findByEmail;
    }
}
