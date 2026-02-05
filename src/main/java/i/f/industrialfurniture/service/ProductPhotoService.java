package i.f.industrialfurniture.service;

import i.f.industrialfurniture.dto.user.GetPhotoDto;

import java.util.List;
import java.util.Map;

public interface ProductPhotoService {
    Map<Integer, GetPhotoDto> getPhotosForProducts(List<Integer> productIds);
}
