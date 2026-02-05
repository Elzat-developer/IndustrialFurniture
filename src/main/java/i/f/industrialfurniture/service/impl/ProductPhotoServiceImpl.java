package i.f.industrialfurniture.service.impl;

import i.f.industrialfurniture.dto.user.GetPhotoDto;
import i.f.industrialfurniture.model.entity.ProductImage;
import i.f.industrialfurniture.repositories.ProductImageRepository;
import i.f.industrialfurniture.service.ProductPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductPhotoServiceImpl implements ProductPhotoService {
    private final ProductImageRepository productImageRepository;
    @Override
    public Map<Integer, GetPhotoDto> getPhotosForProducts(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. Достаем все фото для этих продуктов из базы
        List<ProductImage> allImages = productImageRepository.findAllByProductIdIn(productIds);

        // 2. Группируем их в Map, где ключ - productId, а значение - GetPhotoDto
        return allImages.stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(), // Ключ: ID продукта
                        img -> new GetPhotoDto(img.getId(), img.getUrl()), // Значение: DTO с ID фото и URL
                        (existing, replacement) -> existing // Если у продукта несколько фото, берем первое встречное
                ));
    }
}
