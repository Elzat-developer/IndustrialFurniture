package i.f.industrialfurniture.service.impl;

import i.f.industrialfurniture.dto.admin.*;
import i.f.industrialfurniture.dto.order.GetOrdersDto;
import i.f.industrialfurniture.dto.user.CreateProductDto;
import i.f.industrialfurniture.dto.user.GetPhotoDto;
import i.f.industrialfurniture.dto.user.GetProductsUserDto;
import i.f.industrialfurniture.mapper.ProductMapper;
import i.f.industrialfurniture.model.*;
import i.f.industrialfurniture.model.entity.*;
import i.f.industrialfurniture.repositories.*;
import i.f.industrialfurniture.service.AdminService;
import i.f.industrialfurniture.service.ImportHistoryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductMapper productMapper;
    private final TechnicalSpecificationRepo specificationRepo;
    private final CartItemRepo cartItemRepo;
    private final ImportHistoryRepo importHistoryRepo;
    private final OrderRepo orderRepo;
    private final PromotionRepo promotionRepo;
    private final NewsRepo newsRepo;
    private final CompanyRepo companyRepo;
    private final TechnicalSpecificationRepo technicalSpecificationRepo;
    private final ImportHistoryService importHistoryService;
    @Value("${storage.base-path}")
    private String basePath;
    @Value("${storage.dirs.product}")
    private String productDir;
    @Value("${storage.dirs.news}")
    private String newsDir;
    @Value("${storage.dirs.promotion}")
    private String promotionDir;
    @Value("${storage.dirs.tech-spec}")
    private String techSpecDir;
    @Value("${storage.dirs.logo}")
    private String logoDir;
    @Value("${storage.dirs.category}")
    private String categoryDir;
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void createProduct(CreateProductDto createProductDto,List<MultipartFile> photos) {
            Product product = productMapper.createProductFromDto(createProductDto);
            product.setActive(true);
            product.setCreatedAt(LocalDateTime.now());
            if (createProductDto.categoryId() != null){
            Category category = categoryRepo.findById(createProductDto.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category Not Found!"));
                product.setCategory(category);
            }
            if (photos != null && !photos.isEmpty()) {
                setPhotosProduct(photos, product);
            }
            productRepo.save(product);
    }

    @Override
    @Cacheable(value = "products")
    public List<GetProductsDto> getProducts(ProductType productType,Boolean active) {
        // Формируем запрос "на лету"
        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.hasType(productType),
                ProductSpecifications.hasActiveStatus(active));

        // Один запрос в базу, который вернет именно то, что нужно для нажатой кнопки
        return productRepo.findAll(spec).stream()
                .map(this::toProductsAll)
                .toList();
    }

    @Override
    @Transactional
    @Cacheable(value = "products")
    public GetProductDto getProduct(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));
        return toProduct(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void editProduct(EditProductDto editProduct,List<MultipartFile> photos) {
        Product product = productRepo.findById(editProduct.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));

        productMapper.updateProductFromDto(editProduct,product);
        if (editProduct.categoryId() != null) {
            Category category = categoryRepo.findById(editProduct.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category Not Found!"));
            product.setCategory(category);
        }

        product.setUpdatedAt(LocalDateTime.now());

        if (photos != null && !photos.isEmpty()){
            // 1. Сначала удаляем старые файлы с диска
            product.getPhotos().forEach(photo -> deleteFileFromDisk(photo.getUrl()));

            // 2. Очищаем список (orphanRemoval = true в сущности удалит их из БД)
            product.getPhotos().clear();

            // 3. Сохраняем новые сжатые фото
            setPhotosProduct(photos,product);
        }
        if (editProduct.specifications() != null){
            // Очищаем старые характеристики и добавляем новые
            // Это гарантирует, что Hibernate корректно синхронизирует таблицу product_specifications
            product.getSpecifications().clear();
            product.getSpecifications().putAll(editProduct.specifications());
        }
        productRepo.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        cartItemRepo.deleteByProductId(productId);
        product.setActive(false);
        productRepo.save(product);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void createCategory(CreateCategoryDto createCategoryDto) {
        Category category = new Category();
        category.setCategoryName(createCategoryDto.categoryName());
        category.setDescription(createCategoryDto.description());
        category.setCategoryType(createCategoryDto.categoryType());
        setPhotoCategory(createCategoryDto.photoUrl(),category);
        categoryRepo.save(category);
    }

    @Override
    @Cacheable(value = "categories")
    public List<GetCategories> getCategories(CategoryType categoryType,Boolean active) {
        // Формируем запрос "на лету"
        Specification<Category> spec = Specification.allOf(
                CategorySpecifications.hasType(categoryType),
                CategorySpecifications.hasActiveStatus(active)
        );
        return categoryRepo.findAll(spec).stream()
                .map(this::toCategories)
                .toList();
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void editCategory(EditCategoryDto editCategory) {
        Category category = categoryRepo.findById(editCategory.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category Not Found!"));
        if (editCategory.categoryName() != null){
            category.setCategoryName(editCategory.categoryName());
        }
        if (editCategory.description() != null){
            category.setDescription(editCategory.description());
        }
        if (editCategory.categoryType() != null){
            category.setCategoryType(editCategory.categoryType());
        }
        if (editCategory.active() != null){
            category.setActive(editCategory.active());
        }
        if (editCategory.photoUrl() != null && !editCategory.photoUrl().isEmpty()){
            deleteFileFromDisk(category.getPhotoUrl());
            setPhotoCategory(editCategory.photoUrl(),category);
        }
        categoryRepo.save(category);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Integer categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category Not Found!"));
        category.setActive(false);
        categoryRepo.save(category);
    }

    @Override
    public List<GetProductsUserDto> findProducts(Boolean active,ProductType productType, Integer categoryId, String material, BigDecimal minPrice, BigDecimal maxPrice) {
            Specification<Product> spec = Specification.allOf(
                    ProductSpecifications.hasActiveStatus(active),
                ProductSpecifications.hasType(productType),
                ProductSpecifications.hasCategory(categoryId),
                ProductSpecifications.hasMaterial(material),
                ProductSpecifications.priceBetween(minPrice, maxPrice)
            );

        return productRepo.findAll(spec).stream()
                .map(this::toProductUser)
                .toList();
    }

    private GetProductsUserDto toProductUser(Product product) {
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
                product.isActive(),
                photoDto
        );
    }

    @Override
    @CacheEvict(value = "technical_specifications",allEntries = true)
    public void createTechSpec(CreateTechSpec createTechSpec) {
        TechnicalSpecification specification = new TechnicalSpecification();
        specification.setFileName(createTechSpec.fileName());
        if (createTechSpec.product_id() != null){
            Product product = productRepo.findById(createTechSpec.product_id())
                    .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));
            specification.setProduct(product);
        }
        setFileUrl(createTechSpec, specification);
        specificationRepo.save(specification);
        log.info("Тех Спецификация успешно создана для продукта ID={}", createTechSpec.product_id());
    }

    @Override
    @Cacheable(value = "technical_specifications")
    public List<GetTechSpecDto> getTechSpecs() {
        List<TechnicalSpecification> specifications = specificationRepo.findAll();
        return specifications.stream()
                .map(this::toTechSpec)
                .toList();
    }

    @Override
    @CacheEvict(value = "technical_specifications",allEntries = true)
    public void editTechSpec(Integer tech_spec_id,EditTechSpec techSpecDto) {
        TechnicalSpecification specification = specificationRepo.findById(tech_spec_id)
                .orElseThrow(() -> new IllegalArgumentException("Tech Spec Not Found!"));

        if (techSpecDto.fileName() != null && !techSpecDto.fileName().trim().isEmpty()){
            specification.setFileName(techSpecDto.fileName());
        }
        if (techSpecDto.product_id() != null){
            Product product = productRepo.findById(techSpecDto.product_id())
                    .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));
            specification.setProduct(product);
        }
        if (techSpecDto.fileTechSpec() != null && !techSpecDto.fileTechSpec().isEmpty()) {
            deleteFileFromDisk(specification.getFileUrl());
            setFileUrl(techSpecDto, specification);
        }
        specificationRepo.save(specification);
        log.info("Тех Спецификация успешно обновлена для продукта ID={}", tech_spec_id);
    }

    private void setFileUrl(EditTechSpec techSpecDto, TechnicalSpecification specification) {
        Path uploadDir = Paths.get(basePath, techSpecDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("Ошибка при создании директории для технических спецификаций: {}", e.getMessage());
            throw new RuntimeException("Ошибка при создании директории", e);
        }
        if (techSpecDto.fileTechSpec() != null && !techSpecDto.fileTechSpec().isEmpty()){
            String techSpecPath = processMultipartFile(techSpecDto.fileTechSpec(), uploadDir);
            specification.setFileUrl(techSpecPath);
            log.info("✅ ТехСпек успешно обновлен: {}", techSpecPath);
        }
    }

    @Override
    @CacheEvict(value = "technical_specifications",allEntries = true)
    public void deleteTechSpec(Integer techSpecId) {
        TechnicalSpecification technicalSpecification = technicalSpecificationRepo.findById(techSpecId)
                .orElseThrow(() -> new IllegalArgumentException("TechnicalSpecification Not Found!"));
        if (technicalSpecification.getFileUrl() != null){
            deleteFileFromDisk(technicalSpecification.getFileUrl());
        }
        specificationRepo.delete(technicalSpecification);
    }

    @Override
    @Cacheable(value = "orders")
    public List<GetOrdersDto> getOrders() {
        List<Order> order = orderRepo.findAll();
        return order.stream()
                .map(this::toOrdersAll)
                .toList();
    }

    @Override
    @CacheEvict(value = "orders",allEntries = true)
    public void editPaidStatusOrder(Integer orderId, PaidStatus paidStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setPaidStatus(paidStatus);
        orderRepo.save(order);
    }

    @Override
    @CacheEvict(value = "orders",allEntries = true)
    public void deleteOrder(Integer orderId) {
        orderRepo.deleteById(orderId);
    }

    @Override
    @CacheEvict(value = "promotions",allEntries = true)
    public void createPromotion(MultipartFile urlPhoto) {
        if (!urlPhoto.isEmpty()) {
            Promotion promotion = new Promotion();
            setPhotoPromotion(urlPhoto, promotion);
            promotionRepo.save(promotion);
        }
    }

    @Override
    @CacheEvict(value = "promotions",allEntries = true)
    public void editPromotion(Integer promotionId, MultipartFile urlPhoto) {
        if (urlPhoto != null && !urlPhoto.isEmpty()) {
            Promotion promotion = promotionRepo.findById(promotionId)
                    .orElseThrow(() -> new IllegalArgumentException("Promotion Not Found"));
            deleteFileFromDisk(promotion.getUrlPhoto());
            setPhotoPromotion(urlPhoto, promotion);
            promotionRepo.save(promotion);
        }
    }

    @Override
    @CacheEvict(value = "promotions",allEntries = true)
    public void deletePromotion(Integer promotionId) {
        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion Not Found"));
        if (promotion.getUrlPhoto() != null) {
            deleteFileFromDisk(promotion.getUrlPhoto());
        }
        promotionRepo.delete(promotion);
    }

    @Override
    @CacheEvict(value = "news",allEntries = true)
    public void createNews(CreateNewsDto newsDto) {
        News news = new News();
        news.setName(newsDto.name());
        news.setDescription(newsDto.description());
        news.setCreateDateNews(LocalDateTime.now());
        setPhotoNews(newsDto.newsPhotoUrl(),news);
        newsRepo.save(news);
    }

    @Override
    @CacheEvict(value = "news",allEntries = true)
    public void editNews(Integer newsId, CreateNewsDto editNews) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("News Not Found"));
        if (editNews.name() != null){
            news.setName(editNews.name());
        }
        if (editNews.description() != null){
            news.setDescription(editNews.description());
        }
        if (editNews.newsPhotoUrl() != null && !editNews.newsPhotoUrl().isEmpty()){
            deleteFileFromDisk(news.getNewsPhotoUrl());
            setPhotoNews(editNews.newsPhotoUrl(),news);
        }
        newsRepo.save(news);
    }

    @Override
    @CacheEvict(value = "news",allEntries = true)
    public void deleteNews(Integer newsId) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("News Not Found"));
        deleteFileFromDisk(news.getNewsPhotoUrl());
        newsRepo.delete(news);
    }

    @Override
    @CacheEvict(value = "companies",allEntries = true)
    public void editCompany(CreateCompanyDto editCompany) {
        Company company = companyRepo.findById(1)
                .orElseThrow(() -> new IllegalArgumentException("Company Not Found"));
        if (editCompany.name() != null) {
            company.setName(editCompany.name());
        }
        if (editCompany.text() != null) {
            company.setText(editCompany.text());
        }
        if (editCompany.email() != null) {
            company.setEmail(editCompany.email());
        }
        if (editCompany.phone() != null) {
            company.setPhone(editCompany.phone());
        }
        if (editCompany.address() != null) {
            company.setAddress(editCompany.address());
        }
        if (editCompany.requisites() != null) {
            company.setRequisites(editCompany.requisites());
        }
        if (editCompany.jobStartAndEndDate() != null) {
            company.setJobStartAndEndDate(editCompany.jobStartAndEndDate());
        }
        if (editCompany.logoUrl() != null && !editCompany.logoUrl().isEmpty()) {
            deleteFileFromDisk(company.getLogoUrl());
            setLogoCompany(editCompany.logoUrl(), company);
        }
        companyRepo.save(company);
    }

    @Override
    @Transactional
    public ImportReportDto importProductsFromZip(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int batchSize = 30; // Должно совпадать с настройкой в properties
        Path tempDir = Paths.get(basePath, "temp_import_" + UUID.randomUUID());

        try {
            Files.createDirectories(tempDir);
            unzip(file, tempDir);

            Path excelPath = Files.walk(tempDir)
                    .filter(p -> p.toString().endsWith(".xlsx"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Excel файл не найден"));

            try (InputStream is = Files.newInputStream(excelPath);
                 Workbook workbook = new XSSFWorkbook(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0); // Читаем шапку

                Map<Integer, Category> categoryCache = categoryRepo.findAll()
                        .stream().collect(Collectors.toMap(Category::getId, c -> c));

                // Проходим по строкам (данные начинаются с i=1)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || isRowEmpty(row)) continue;

                    try {
                        Product product = processRowToProduct(row, headerRow, categoryCache, tempDir, errors, i);
                        // Вместо productRepo.save() используем persist
                        entityManager.persist(product);
                        successCount++;

                        // Сбрасываем пакет в БД каждые batchSize записей
                        if (successCount % batchSize == 0) {
                            entityManager.flush();
                            entityManager.clear();
                            // clear() очищает кэш L1, освобождая те самые 2ГБ ОЗУ
                        }

                    } catch (Exception e) {
                        errors.add("Строка " + (i + 1) + ": " + e.getMessage());
                    }
                }
            }
            // Финальный сброс остатков
            entityManager.flush();
            entityManager.clear();
            // 2. Если всё прошло успешно (или с ошибками в строках), сохраняем финальный отчет
            importHistoryService.saveImportHistory(file.getOriginalFilename(), successCount, errors);
        } catch (Exception e) {
            log.error("❌ Ошибка импорта: ", e);
            // Даже если случилась катастрофа, пытаемся сохранить то, что успели насчитать
            importHistoryService.saveImportHistory(file.getOriginalFilename(), successCount,
                    List.of("Критическая ошибка: " + e.getMessage()));
            throw new RuntimeException("Ошибка импорта: " + e.getMessage());
        } finally {
            FileUtils.deleteQuietly(tempDir.toFile());
        }


        return new ImportReportDto(successCount, errors.size(), errors);
    }
    private Product processRowToProduct(Row row, Row headerRow, Map<Integer, Category> categoryCache,
                                        Path tempDir, List<String> errors, int i) {
        Product product = new Product();

        // 1. Базовые поля (индексы 0-7)
        product.setProductName(getSafeString(row, 0));
        product.setTag(getSafeString(row, 1));
        product.setPrice(getSafeBigDecimal(row, 2));
        product.setMaterial(getSafeString(row, 3));
        product.setDimensions(getSafeString(row, 4));
        product.setWeight(getSafeDouble(row, 5));
        product.setQuantity(getSafeInt(row, 6));

        // Категория
        Integer catId = getSafeInt(row, 7);
        if (catId != null && categoryCache.containsKey(catId)) {
            product.setCategory(categoryCache.get(catId));
        } else {
            throw new RuntimeException("Категория с ID " + catId + " не найдена");
        }

        // 2. Новые фиксированные поля (индексы 9-15)
        product.setDescription(getSafeString(row, 9));
        product.setWidth(getSafeInt(row, 10));
        product.setDepth(getSafeInt(row, 11));
        product.setHeight(getSafeInt(row, 12));
        product.setPower(getSafeString(row, 13));
        product.setVoltage(getSafeString(row, 14));
        product.setCountry(getSafeString(row, 15));

        // 3. Обработка Enum ProductType (индекс 16)
        String typeStr = getSafeString(row, 16).trim().toLowerCase();
        if (typeStr.equals("industrial") || typeStr.equals("промышленный")) {
            product.setProductType(ProductType.industrial);
        } else if (typeStr.equals("household") || typeStr.equals("бытовой")) {
            product.setProductType(ProductType.household);
        } else {
            product.setProductType(ProductType.industrial); // Значение по умолчанию
        }

        // 4. Динамические характеристики (начиная с 17 колонки)
        // Мы идем до конца строки в заголовке
        for (int cellIdx = 17; cellIdx < headerRow.getLastCellNum(); cellIdx++) {
            String headerName = getSafeString(headerRow, cellIdx);
            String cellValue = getSafeString(row, cellIdx);

            if (headerName.startsWith("Спец:") && !cellValue.isBlank()) {
                // Убираем "Спец:" и лишние пробелы из ключа
                String specKey = headerName.substring(5).trim();
                product.getSpecifications().put(specKey, cellValue);
            }
        }

        // 5. Обработка фото (твой метод, индекс 8)
        processPhotos(row, 8, tempDir, product, errors, i);

        // Служебные поля
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());

        return product;
    }
    // Вынес логику фото в отдельный метод для чистоты
    private void processPhotos(Row row, int cellIdx, Path tempDir, Product product, List<String> errors, int rowNum) {
        String photosString = getSafeString(row, cellIdx);
        if (!photosString.isBlank()) {
            String[] photoNames = photosString.split(",");
            for (String photoName : photoNames) {
                Path sourcePhotoPath = tempDir.resolve("images").resolve(photoName.trim());
                if (Files.exists(sourcePhotoPath)) {
                    try {
                        String savedPath = processImageFile(sourcePhotoPath, Paths.get(basePath, productDir));
                        ProductImage pi = new ProductImage();
                        pi.setUrl(savedPath);
                        pi.setProduct(product);
                        product.getPhotos().add(pi);
                    } catch (IOException e) {
                        errors.add("Строка " + (rowNum + 1) + ": Ошибка сжатия " + photoName);
                    }
                } else {
                    errors.add("Строка " + (rowNum + 1) + ": Файл " + photoName + " не найден");
                }
            }
        }
    }
    private String processImageFile(Path sourcePath, Path uploadDir) throws IOException {
        String originalFilename = sourcePath.getFileName().toString();
        String baseName = UUID.randomUUID().toString();

        // 1. Извлекаем расширение
        String extension = "jpg";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        String fileName = baseName + "." + extension;
        Path filePath = uploadDir.resolve(fileName);

        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }
        try {
            if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
                log.info("📸 Обработка изображения: {} (формат: {})", originalFilename, extension);
                // 3. Для PNG, JPG и прочих используем сжатие
                net.coobird.thumbnailator.Thumbnails.of(sourcePath.toFile())
                        .size(1600, 1600)
                        .outputQuality(0.8)
                        .outputFormat(extension) // Сохраняем оригинальный формат (png -> png, jpg -> jpg)
                        .toFile(filePath.toFile());
                // Возвращаем путь для сохранения в БД (относительный)
                return filePath.toString();
                // 2. Специальная обработка для WebP (если Thumbnailator его не съест)
            } else if (extension.equals("webp")) {
                    log.info("📄 WebP обнаружен, сохраняем как есть (без пережатия Thumbnailator)");
                    Files.copy(sourcePath, filePath, StandardCopyOption.REPLACE_EXISTING);
                    return filePath.toString();
            } else {
                // Если не картинка
                log.info("📄 Сохраняем файл без изменений: {}", originalFilename);
                fileName = baseName + "_" + originalFilename;
                filePath = uploadDir.resolve(fileName);
                Files.copy(sourcePath, filePath, StandardCopyOption.REPLACE_EXISTING);
                return filePath.toString();
            }
        } catch (Exception e) {
        log.error("❌ Ошибка при обработке фото {}: {}", originalFilename, e.getMessage());
        throw new IOException("Не удалось обработать файл изображения", e);
    }
}
    private String getSafeString(Row row, int cellIdx) {
        Cell cell = row.getCell(cellIdx);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue().trim();
            }
            case NUMERIC -> {
                // Проверяем, не дата ли это (на всякий случай)
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Убираем лишние ".0" у целых чисел
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            }
            case BOOLEAN -> {
                return String.valueOf(cell.getBooleanCellValue());
            }
            case FORMULA -> {
                // Пытаемся получить строковое значение формулы, если не выйдет - берем результат
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> {
                return "";
            }
        }
    }

    private Integer getSafeInt(Row row, int cellIdx) {
        Cell cell = row.getCell(cellIdx);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            return null;
        }
        return (int) cell.getNumericCellValue();
    }

    private BigDecimal getSafeBigDecimal(Row row, int cellIdx) {
        Cell cell = row.getCell(cellIdx);
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue().replace(",", "."));
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    @Override
    @CacheEvict(value = "products",allEntries = true)
    public void editProductActive(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));
        product.setActive(true);
        productRepo.save(product);
    }

    @Override
    @CacheEvict(value = "categories",allEntries = true)
    public void editCategoryActive(Integer categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category Not Found!"));
        category.setActive(true);
        categoryRepo.save(category);
    }

    @Override
    public List<GetProductsDto> findProductsAdmin(Boolean active, ProductType productType, Integer categoryId, String material, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.hasActiveStatus(active),
                ProductSpecifications.hasType(productType),
                ProductSpecifications.hasCategory(categoryId),
                ProductSpecifications.hasMaterial(material),
                ProductSpecifications.priceBetween(minPrice, maxPrice)
        );

        return productRepo.findAll(spec).stream()
                .map(this::toProductsAll)
                .toList();
    }

    @Override
    public List<ImportHistoriesDto> getImportHistories() {
        List<ImportHistory> importHistories = importHistoryRepo.findAll();
        return importHistories.stream()
                .map(this::toImportHistory)
                .toList();
    }

    private ImportHistoriesDto toImportHistory(ImportHistory importHistory) {
        return new ImportHistoriesDto(
                importHistory.getId(),
                importHistory.getFileName(),
                importHistory.getImportStatus(),
                importHistory.getCreatedAt()
        );
    }

    @Override
    public ImportHistoryDto getImportHistory(Integer historyId) {
        ImportHistory importHistory = importHistoryRepo.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("ImportHistory Not Found"));
        return new ImportHistoryDto(
                importHistory.getId(),
                importHistory.getFileName(),
                importHistory.getSuccessCount(),
                importHistory.getErrorCount(),
                importHistory.getImportStatus(),
                importHistory.getErrorsLog(),
                importHistory.getCreatedAt()
        );
    }

    @Override
    public void deleteImportHistory(Integer historyId) {
        importHistoryRepo.deleteById(historyId);
    }

    private void unzip(MultipartFile zipFile, Path targetDir) throws IOException {
        // Добавляем Charset.forName("CP866") — это решит проблему с "malformed input"
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(), Charset.forName("CP866"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                // Защита от Zip Slip vulnerability
                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Entry is outside of the target dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
    private Double getSafeDouble(Row row, int cellIdx) {
        Cell cell = row.getCell(cellIdx);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return 0.0;
        }

        // Если это число, просто возвращаем его
        if (cell.getCellType() == CellType.NUMERIC) {
            // Проверка на дату (тот самый случай "25.янв")
            if (DateUtil.isCellDateFormatted(cell)) {
                log.warn("В колонке {} (вес) обнаружена дата вместо числа. Проверьте формат в Excel!", cellIdx);
            }
            return cell.getNumericCellValue();
        }

        // Если вдруг в ячейке текст (например, "7.5"), пытаемся спарсить
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
            } catch (Exception e) {
                return 0.0;
            }
        }

        return 0.0;
    }
    private void setLogoCompany(MultipartFile logoUrl, Company company) {
        Path uploadDir = Paths.get(basePath, logoDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }
        String photoPromotionPath = processMultipartFile(logoUrl, uploadDir);
        company.setLogoUrl(photoPromotionPath);
        log.info("✅ Promotion успешно сохранено: {}", photoPromotionPath);
    }
    private void setPhotoCategory(MultipartFile photoUrl, Category category) {
        Path uploadDir = Paths.get(basePath, categoryDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }
        String photoPromotionPath = processMultipartFile(photoUrl, uploadDir);
        category.setPhotoUrl(photoPromotionPath);
        log.info("✅ Promotion успешно сохранено: {}", photoPromotionPath);
    }
    private void setPhotoNews(MultipartFile newsPhotoUrl, News news) {
        Path uploadDir = Paths.get(basePath, newsDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }
        String photoPromotionPath = processMultipartFile(newsPhotoUrl, uploadDir);
        news.setNewsPhotoUrl(photoPromotionPath);
        log.info("✅ Promotion успешно сохранено: {}", photoPromotionPath);
    }

    private void setPhotosProduct(List<MultipartFile> photos, Product product) {
        Path uploadDir = Paths.get(basePath, productDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 1. Проходим циклом по всем пришедшим файлам
        for (MultipartFile file : photos) {
            if (file != null && !file.isEmpty()) {
                // 2. Сохраняем физический файл на диск и получаем путь (как ты делал для новостей)
                String photoPath = processMultipartFile(file, uploadDir);

                // 3. Создаем новый объект сущности для изображения
                // (Предположим, она называется ProductImage)
                ProductImage productImage = new ProductImage();
                productImage.setUrl(photoPath);
                productImage.setProduct(product); // Привязываем картинку к нашему продукту

                // 4. Добавляем это изображение в список изображений продукта
                // Убедись, что в классе Product список инициализирован: new ArrayList<>()
                product.getPhotos().add(productImage);

                log.info("✅ Фото продукта сохранено: {}", photoPath);
            }
        }
    }

    private void deleteFileFromDisk(String urlPhoto) {
        // 1. Проверка на null или пустоту, чтобы не тратить ресурсы
        if (urlPhoto == null || urlPhoto.isBlank()) {
            log.warn("⚠️ Попытка удалить пустой путь к файлу.");
            return;
        }

        try {
            Path path = Paths.get(urlPhoto);

            // 2. deleteIfExists — идеальный метод.
            // Если файла нет (например, удалили вручную), он просто вернет false без ошибки.
            boolean deleted = Files.deleteIfExists(path);

            if (deleted) {
                log.info("🗑️ Файл успешно удален: {}", urlPhoto);
            } else {
                log.warn("🔍 Файл не найден на диске, удаление пропущено: {}", urlPhoto);
            }
        } catch (IOException e) {
            // Логируем ошибку, но не прерываем работу программы (чтобы админ мог доделать правки)
            log.error("❌ Ошибка при удалении файла {}: {}", urlPhoto, e.getMessage());
        }
    }

    private void setPhotoPromotion(MultipartFile urlPhoto, Promotion promotion) {
        Path uploadDir = Paths.get(basePath, promotionDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }
        String photoPromotionPath = processMultipartFile(urlPhoto, uploadDir);
        promotion.setUrlPhoto(photoPromotionPath);
        log.info("✅ Promotion успешно сохранено: {}", photoPromotionPath);
    }
    private void setFileUrl(CreateTechSpec createTechSpec, TechnicalSpecification specification) {
        Path uploadDir = Paths.get(basePath, techSpecDir);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }
        if (!createTechSpec.fileTechSpec().isEmpty()){
            String techSpecPath = processMultipartFile(createTechSpec.fileTechSpec(), uploadDir);
            specification.setFileUrl(techSpecPath);
            log.info("✅ ТехСпек успешно сохранено: {}", techSpecPath);
        }
    }
    private GetOrdersDto toOrdersAll(Order order) {
        return new GetOrdersDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getOrderStartDate(),
                order.getTotalPrice(),
                order.getPaidStatus()
        );
    }

    private boolean isRowEmpty(Row row) {
        Cell firstCell = row.getCell(0);
        return firstCell == null || firstCell.getCellType() == CellType.BLANK;
    }
    private GetTechSpecDto toTechSpec(TechnicalSpecification technicalSpecification) {
        Integer productId = null;
        if (technicalSpecification.getProduct().getId() != null){
            productId = technicalSpecification.getProduct().getId();
        }
        return new GetTechSpecDto(
                technicalSpecification.getId() == null ? null : technicalSpecification.getId(),
                technicalSpecification.getFileName(),
                technicalSpecification.getFileUrl(),
                productId
        );
    }

    private String processMultipartFile(MultipartFile multipartFile, Path uploadDir) {
        String originalFilename = multipartFile.getOriginalFilename();
        String baseName = UUID.randomUUID().toString();
        String contentType = multipartFile.getContentType();//

        // 1. Извлекаем расширение (например, "png", "jpg", "webp")
        String extension = "jpg"; // значение по умолчанию
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        try {
            if (contentType.startsWith("image/")) {
                log.info("📸 Обработка изображения: {} (формат: {})", originalFilename, extension);

                String fileName = baseName + "." + extension;
                Path filePath = uploadDir.resolve(fileName);
                Files.createDirectories(uploadDir);

                // 2. Специальная обработка для WebP (если Thumbnailator его не съест)
                if (extension.equals("webp")) {
                    log.info("📄 WebP обнаружен, сохраняем как есть (без пережатия Thumbnailator)");
                    multipartFile.transferTo(filePath);
                } else {
                    // 3. Для PNG, JPG и прочих используем сжатие
                    net.coobird.thumbnailator.Thumbnails.of(multipartFile.getInputStream())
                            .size(1600, 1600)
                            .outputQuality(0.8)
                            .outputFormat(extension) // Сохраняем оригинальный формат (png -> png, jpg -> jpg)
                            .toFile(filePath.toFile());
                }

                // Возвращаем путь для сохранения в БД (относительный)
                return filePath.toString();
            } else {
                // Если не картинка
                String fileName = baseName + "_" + originalFilename;
                Path filePath = uploadDir.resolve(fileName);
                log.info("📄 Сохраняем файл без изменений: {}", originalFilename);
                Files.createDirectories(uploadDir);
                multipartFile.transferTo(filePath);
                return filePath.toString();
            }
        } catch (IOException e) {
            log.error("❌ Ошибка при сохранении '{}': {}", originalFilename, e.getMessage());
            throw new RuntimeException("Ошибка при обработке файла", e);
        }
    }

    private GetCategories toCategories(Category category) {
        return new GetCategories(
                category.getId() == null ? null : category.getId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getPhotoUrl(),
                category.getCategoryType()
        );
    }

    private GetProductDto toProduct(Product product) {
        // Используем Optional для более лаконичного извлечения ID категории
        Integer categoryId = Optional.ofNullable(product.getCategory())
                .map(Category::getId)
                .orElse(null);

        List<GetPhotoDto> photos = Optional.ofNullable(product.getPhotos())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toProductPhoto)
                .toList();

        // Защита от изменений оригинальной карты
        Map<String, String> specs = product.getSpecifications() != null
                ? new HashMap<>(product.getSpecifications())
                : Collections.emptyMap();

        var techSpec = Optional.ofNullable(product.getTechnicalSpecification());

        String tsUrl = techSpec.map(TechnicalSpecification::getFileUrl).orElse(null);
        String tsName = techSpec.map(TechnicalSpecification::getFileName).orElse(null);

        return new GetProductDto(
                product.getId(), // Если id в базе Integer, проверка на null тут избыточна, JPA вернет либо id, либо упадет раньше
                product.getProductName(),
                product.getDescription(),
                product.getTag(),
                product.getPrice(),
                product.getMaterial(),
                product.getDimensions(),
                product.getWeight(),
                product.getWidth(),
                product.getDepth(),
                product.getHeight(),
                product.getPower(),
                product.getVoltage(),
                product.getCountry(),
                specs,
                product.getCreatedAt(),
                product.getUpdatedAt(),
                categoryId,
                product.getQuantity(),
                product.getProductType(),
                photos,
                tsUrl,
                tsName
        );
    }

    private GetPhotoDto toProductPhoto(ProductImage productImage) {
        return new GetPhotoDto(
                productImage.getId() == null ? null : productImage.getId(),
                productImage.getUrl()
        );
    }

    private GetProductsDto toProductsAll(Product product) {
        GetPhotoDto photoDto = null;

        // Проверяем есть ли у продукта фото
        if (product.getPhotos() != null && !product.getPhotos().isEmpty()) {
            ProductImage firstPhoto = product.getPhotos().get(0);
            photoDto = new GetPhotoDto(
                    firstPhoto.getId() == null ? null : firstPhoto.getId(),
                    firstPhoto.getUrl()
            );
        }

        return new GetProductsDto(
                product.getId() == null ? null : product.getId(),
                product.getProductName(),
                product.getTag(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getMaterial(),
                product.getCategory() != null && product.getCategory().getId() != null
                        ? product.getCategory().getId() : null,
                product.getProductType(),
                photoDto
        );
    }
}
