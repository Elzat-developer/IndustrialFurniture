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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Override
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
    public List<GetProductsDto> getProducts(ProductType productType,Boolean active) {
        // Формируем запрос "на лету"
        Specification<Product> spec = Specification.where(
                ProductSpecifications.hasType(productType))
                .and(ProductSpecifications.hasActiveStatus(active));

        // Один запрос в базу, который вернет именно то, что нужно для нажатой кнопки
        return productRepo.findAll(spec).stream()
                .map(this::toProductsAll)
                .toList();
    }

    @Override
    @Transactional
    public GetProductDto getProduct(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));
        return toProduct(product);
    }

    @Override
    @Transactional
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
    public void deleteProduct(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        cartItemRepo.deleteByProductId(productId);
        product.setActive(false);
        productRepo.save(product);
    }

    @Override
    public void createCategory(CreateCategoryDto createCategoryDto) {
        Category category = new Category();
        category.setCategoryName(createCategoryDto.categoryName());
        category.setDescription(createCategoryDto.description());
        category.setCategoryType(createCategoryDto.categoryType());
        setPhotoCategory(createCategoryDto.photoUrl(),category);
        categoryRepo.save(category);
    }

    @Override
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
                photoDto
        );
    }

    @Override
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
    public List<GetTechSpecDto> getTechSpecs() {
        List<TechnicalSpecification> specifications = specificationRepo.findAll();
        return specifications.stream()
                .map(this::toTechSpec)
                .toList();
    }

    @Override
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
    public void deleteTechSpec(Integer techSpecId) {
        TechnicalSpecification technicalSpecification = technicalSpecificationRepo.findById(techSpecId)
                .orElseThrow(() -> new IllegalArgumentException("TechnicalSpecification Not Found!"));
        if (technicalSpecification.getFileUrl() != null){
            deleteFileFromDisk(technicalSpecification.getFileUrl());
        }
        specificationRepo.delete(technicalSpecification);
    }

    @Override
    public List<GetOrdersDto> getOrders() {
        List<Order> order = orderRepo.findAll();
        return order.stream()
                .map(this::toOrdersAll)
                .toList();
    }

    @Override
    public void editPaidStatusOrder(Integer orderId, PaidStatus paidStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setPaidStatus(paidStatus);
        orderRepo.save(order);
    }

    @Override
    public void deleteOrder(Integer orderId) {
        orderRepo.deleteById(orderId);
    }

    @Override
    public void createPromotion(MultipartFile urlPhoto) {
        if (!urlPhoto.isEmpty()) {
            Promotion promotion = new Promotion();
            setPhotoPromotion(urlPhoto, promotion);
            promotionRepo.save(promotion);
        }
    }

    @Override
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
    public void deletePromotion(Integer promotionId) {
        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion Not Found"));
        if (promotion.getUrlPhoto() != null) {
            deleteFileFromDisk(promotion.getUrlPhoto());
        }
        promotionRepo.delete(promotion);
    }

    @Override
    public void createNews(CreateNewsDto newsDto) {
        News news = new News();
        news.setName(newsDto.name());
        news.setDescription(newsDto.description());
        news.setCreateDateNews(LocalDateTime.now());
        setPhotoNews(newsDto.newsPhotoUrl(),news);
        newsRepo.save(news);
    }

    @Override
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
    public void deleteNews(Integer newsId) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("News Not Found"));
        deleteFileFromDisk(news.getNewsPhotoUrl());
        newsRepo.delete(news);
    }

    @Override
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

        // 1. Создаем временную папку для распаковки
        Path tempDir = Paths.get(basePath, "temp_import_" + UUID.randomUUID());

        try {
            Files.createDirectories(tempDir);

            // 2. Распаковываем архив
            unzip(file, tempDir);

            // 3. Ищем Excel файл в корне архива
            Path excelPath = Files.walk(tempDir)
                    .filter(p -> p.toString().endsWith(".xlsx"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Excel файл (.xlsx) не найден в архиве!"));

            try (InputStream is = Files.newInputStream(excelPath);
                 Workbook workbook = new XSSFWorkbook(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                Map<Integer, Category> categoryCache = categoryRepo.findAll()
                        .stream().collect(Collectors.toMap(Category::getId, c -> c));

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || isRowEmpty(row)) continue;

                    try {
                        validateRow(row, categoryCache);

                        Product product = new Product();
                        product.setProductName(getCellValueAsString(row.getCell(0)));
                        product.setTag(getCellValueAsString(row.getCell(1)));
                        product.setPrice(BigDecimal.valueOf(row.getCell(2).getNumericCellValue()));
                        product.setMaterial(getCellValueAsString(row.getCell(3)));
                        product.setDimensions(getCellValueAsString(row.getCell(4)));
                        product.setWeight(row.getCell(5).getNumericCellValue());
                        product.setQuantity((int) row.getCell(6).getNumericCellValue());

                        Integer catId = (int) row.getCell(7).getNumericCellValue();
                        product.setCategory(categoryCache.get(catId));
                        product.setActive(true);
                        product.setCreatedAt(LocalDateTime.now());

                        // --- ЛОГИКА ФОТО (Колонка №8) ---
                        String photosString = getCellValueAsString(row.getCell(8));
                        if (!photosString.isBlank()) {
                            String[] photoNames = photosString.split(",");
                            for (String photoName : photoNames) {
                                photoName = photoName.trim();
                                // Ищем файл в папке images/ внутри архива
                                Path sourcePhotoPath = tempDir.resolve("images").resolve(photoName);

                                if (Files.exists(sourcePhotoPath)) {
                                    String savedPath = compressionPhotoFromPath(sourcePhotoPath, Paths.get(basePath, productDir));

                                    ProductImage pi = new ProductImage();
                                    pi.setUrl(savedPath);
                                    pi.setProduct(product);
                                    product.getPhotos().add(pi);
                                } else {
                                    errors.add("Строка " + (i + 1) + ": Файл " + photoName + " не найден в папке images/");
                                }
                            }
                        }

                        productRepo.save(product);
                        successCount++;

                    } catch (Exception e) {
                        errors.add("Строка " + (i + 1) + ": " + e.getMessage());
                    }
                }
            }

            // Записываем историю (как делали раньше)
            saveImportHistory(file.getOriginalFilename(), successCount, errors);

        } catch (Exception e) {
            log.error("❌ Ошибка импорта из ZIP: ", e);
            throw new RuntimeException("Ошибка импорта: " + e.getMessage());
        } finally {
            // 4. Чистим за собой временные файлы (ОБЯЗАТЕЛЬНО для 2ГБ ОЗУ)
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                log.error("Не удалось удалить временную папку: {}", tempDir);
            }
        }

        return new ImportReportDto(successCount, errors.size(), errors);
    }

    @Override
    public void editProductActive(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product Not Found!"));
        product.setActive(true);
        productRepo.save(product);
    }

    @Override
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

    private void saveImportHistory(String fileName, int success, List<String> errors) {
        ImportHistory history = new ImportHistory();
        history.setFileName(fileName);
        history.setSuccessCount(success);
        history.setErrorCount(errors.size());
        history.setImportStatus(errors.isEmpty() ? ImportStatus.SUCCESS : (success > 0 ? ImportStatus.PARTIAL : ImportStatus.FAILED));
        history.setErrorsLog(String.join("\n", errors));
        history.setCreatedAt(LocalDateTime.now());
        importHistoryRepo.save(history);
    }

    private String compressionPhotoFromPath(Path sourcePath, Path uploadDir) throws IOException {
        Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID() + "_" + sourcePath.getFileName().toString();
        Path targetPath = uploadDir.resolve(fileName);

        log.info("📸 Сжимаем фото из архива: {}", sourcePath.getFileName());

        net.coobird.thumbnailator.Thumbnails.of(sourcePath.toFile())
                .size(1600, 1600)
                .outputQuality(0.8)
                .toFile(targetPath.toFile());

        return targetPath.toString();
    }

    private void unzip(MultipartFile zipFile, Path targetDir) throws IOException {
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(zipFile.getInputStream())) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName());
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

// --- Вспомогательные методы ---

    private void validateRow(Row row, Map<Integer, Category> cache) {
        if (row.getCell(0) == null || getCellValueAsString(row.getCell(0)).isBlank()) {
            throw new IllegalArgumentException("Название товара пустое");
        }
        if (row.getCell(2) == null || row.getCell(2).getCellType() != CellType.NUMERIC) {
            throw new IllegalArgumentException("Цена должна быть числом");
        }
        if (row.getCell(7) == null || row.getCell(7).getCellType() != CellType.NUMERIC) {
            throw new IllegalArgumentException("ID категории должен быть числом");
        }
        int catId = (int) row.getCell(7).getNumericCellValue();
        if (!cache.containsKey(catId)) {
            throw new IllegalArgumentException("Категория с ID " + catId + " не найдена в базе");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
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
        // 1. Генерируем базовое имя без расширения
        String baseName = UUID.randomUUID().toString();
        String contentType = multipartFile.getContentType();

        try {
            if (contentType.startsWith("image/")) {
                log.info("📸 Сжимаем изображение: {}", originalFilename);

                // 2. Всегда сохраняем как .jpg для максимальной совместимости
                String fileName = baseName + ".jpg";
                Path filePath = uploadDir.resolve(fileName);

                net.coobird.thumbnailator.Thumbnails.of(multipartFile.getInputStream())
                        .size(1600, 1600)
                        .outputQuality(0.8)
                        .outputFormat("jpg") // !!! Явно указываем формат для записи
                        .toFile(filePath.toFile());

                return filePath.toString();
            } else {
                // Если это не картинка (например, PDF), сохраняем с оригинальным расширением
                String fileName = baseName + "_" + originalFilename;
                Path filePath = uploadDir.resolve(fileName);
                log.info("📄 Сохраняем файл без сжатия: {}", originalFilename);
                multipartFile.transferTo(filePath);
                return filePath.toString();
            }
        } catch (IOException e) {
            log.error("❌ Ошибка при сохранении или сжатии '{}': {}", originalFilename, e.getMessage(), e);
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
                photos
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
