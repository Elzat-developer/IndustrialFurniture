package i.f.industrialfurniture.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import i.f.industrialfurniture.dto.order.*;
import i.f.industrialfurniture.dto.user.*;
import i.f.industrialfurniture.model.CategoryType;
import i.f.industrialfurniture.model.PaidStatus;
import i.f.industrialfurniture.model.ProductType;
import i.f.industrialfurniture.model.entity.*;
import i.f.industrialfurniture.repositories.*;
import i.f.industrialfurniture.service.ProductPhotoService;
import i.f.industrialfurniture.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final TemplateEngine templateEngine;
    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(String cartToken) {
        Cart cart = cartRepo.findByCartToken(cartToken)
                .orElseGet(() -> createNewCart(cartToken));

        List<CartItemDto> cartItemDtoList = cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();

                    // 1. Безопасное получение фото
                    GetPhotoDto photo = null;
                    if (product.getPhotos() != null && !product.getPhotos().isEmpty()) {
                        photo = new GetPhotoDto(
                                product.getPhotos().get(0).getId(),
                                product.getPhotos().get(0).getUrl()
                        );
                    }

                    // 2. Формирование характеристик (объединяем важные поля в текст)
                    String chars = formatCharacteristics(product);

                    return new CartItemDto(
                            item.getId() != null ? item.getId() : null,
                            product.getId() != null ? product.getId() : null,
                            product.getProductName(),
                            item.getQuantity(),
                            product.getPrice(),
                            product.getTag(),
                            product.isActive(),
                            chars,
                            photo,
                            "3 рабочих дня" // Можно зашить в БД или оставить так
                    );
                }).toList();

        BigDecimal total = cartItemDtoList.stream()
                .map(item -> item.productPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(cart.getId(), cartItemDtoList, total);
    }
    // Вспомогательный метод для красивого вывода характеристик в PDF/Корзине
    private String formatCharacteristics(Product product) {
        StringBuilder sb = new StringBuilder();

        // Вспомогательная функция, чтобы не писать if 10 раз
        addDetail(sb, "Материал", product.getMaterial());
        addDetail(sb, "Мощность", product.getPower());
        addDetail(sb, "Вес", String.valueOf(product.getWeight()));
        addDetail(sb, "Напряжение", product.getVoltage());

        // Габариты лучше объединить в одну строку
        if (product.getWidth() != null || product.getDepth() != null || product.getHeight() != null) {
            sb.append("Габариты: ")
                    .append(product.getWidth()).append("x")
                    .append(product.getDepth()).append("x")
                    .append(product.getHeight()).append(" мм; ");
        }

        // Обработка Map (Specifications)
        if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
            product.getSpecifications().forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    sb.append(key).append(": ").append(value).append("; ");
                }
            });
        }

        addDetail(sb, "Страна", product.getCountry());

        return sb.toString().trim();
    }

    // Утилитарный метод для чистоты кода
    private void addDetail(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append("; ");
        }
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
    public List<GetCategoriesUserDto> getCategories(CategoryType categoryType) {
        List<Category> categories = categoryRepo.findAllByCategoryType(categoryType);
        return categories.stream()
                .map(this::toCategory)
                .toList();
    }

    @Override
    public List<GetProductsUserDto> getProductsUserDto(ProductType productType) {
        List<Product> products = productRepo.findAllByProductType(productType);
        return products.stream()
                .map(this::toProduct)
                .toList();
    }

    @Override
    public byte[] generateCpPdf(List<CartItemDto> items, BigDecimal totalSum) {
        List<Map<String, Object>> pdfItems = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", item.productName());
            map.put("tag", item.tag());
            map.put("characteristics", item.characteristics());
            map.put("quantity", item.quantity());
            map.put("productPrice", item.productPrice());
            map.put("deliveryTerms", item.deliveryTerms());

            // Магия здесь: превращаем URL в Base64 прямо перед генерацией
            if (item.photoDto() != null) {
                map.put("photoBase64", getBase64ImageFromUrl(item.photoDto().photoURL()));
            }

            return map;
        }).toList();

        Context context = new Context();
        context.setVariable("items", pdfItems);
        context.setVariable("totalSum", totalSum);
        context.setVariable("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        String htmlContent = templateEngine.process("cp_template", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Не забываем шрифт для кириллицы!
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/arial.ttf"), "Arial");

            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании PDF", e);
        }
    }
    private String getBase64ImageFromUrl(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) return null;

            // 1. Извлекаем только имя файла из URL (например, из "http://.../uploads/1.jpg" достаем "1.jpg")
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            // 2. Укажи путь к папке, где физически лежат файлы (та, что проброшена в /uploads/**)
            // Если папка uploads лежит в корне проекта:
            Path path = Paths.get("uploads").resolve(fileName).toAbsolutePath();

            if (!Files.exists(path)) {
                System.err.println("Файл не найден по пути: " + path);
                return null;
            }

            // 3. Читаем и конвертируем
            byte[] imageBytes = Files.readAllBytes(path);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            return "data:image/" + extension + ";base64," + base64;
        } catch (Exception e) {
            return null;
        }
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
                product.getMaterial(),
                product.getCategory().getId(),
                product.getProductType(),
                photoDto
        );
    }

    private GetCategoriesUserDto toCategory(Category category) {
        return new GetCategoriesUserDto(
                category.getId(),
                category.getCategoryName(),
                category.getPhotoUrl(),
                category.getCategoryType()
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
