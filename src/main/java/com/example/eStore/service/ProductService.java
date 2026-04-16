package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.ProductRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.ProductImageResponse;
import com.example.eStore.dto.response.ProductResponse;
import com.example.eStore.entity.Brand;
import com.example.eStore.entity.Category;
import com.example.eStore.entity.Product;
import com.example.eStore.entity.ProductImage;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.BrandRepository;
import com.example.eStore.repository.CategoryRepository;
import com.example.eStore.repository.ProductImageRepository;
import com.example.eStore.repository.ProductRepository;
import com.example.eStore.dto.request.ProductImageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final FileUploadService fileUploadService;

    public BaseResultDTO<ProductResponse> create(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(
                        "Category not found",
                        Constants.ErrorCode.Product.CREATE_CATEGORY_NOT_FOUND
                ));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(
                        "Brand not found",
                        Constants.ErrorCode.Product.CREATE_BRAND_NOT_FOUND
                ));

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCpu(request.getCpu());
        product.setRam(request.getRam());
        product.setScreen(request.getScreen());
        product.setOperatingSystem(request.getOperatingSystem());
        product.setBatteryCapacity(request.getBatteryCapacity());
        product.setDesign(request.getDesign());
        product.setWarrantyInfo(request.getWarrantyInfo());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setBrand(brand);

        productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> productImages = request.getImages().stream().map(imgReq -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(imgReq.getImageUrl());
                img.setIsThumbnail(imgReq.getIsThumbnail());
                img.setSortOrder(imgReq.getSortOrder());
                img.setPublicId(imgReq.getPublicId());
                img.setProduct(product);
                return img;
            }).toList();
            productImageRepository.saveAll(productImages);
            product.setImages(productImages);
        }

        return ApiResponseFactory.success(
                Constants.Message.Product.CREATE_SUCCESS,
                mapToResponse(product)
        );
    }

    public BaseResultDTO<Page<ProductResponse>> getAll(String keyword, Pageable pageable) {

        Page<Product> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = productRepository
                    .findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return ApiResponseFactory.success(
                Constants.Message.Product.GET_SUCCESS,
                page.map(this::mapToResponse)
        );
    }

    public BaseResultDTO<ProductResponse> getDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        Constants.ErrorCode.Product.GET_DETAIL_NOT_FOUND
                ));

        return ApiResponseFactory.success(
                Constants.Message.Product.GET_DETAIL_SUCCESS,
                mapToResponse(product)
        );
    }

    public BaseResultDTO<ProductResponse> update(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        Constants.ErrorCode.Product.UPDATE_NOT_FOUND
                ));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(
                        "Category not found",
                        Constants.ErrorCode.Product.UPDATE_CATEGORY_NOT_FOUND
                ));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(
                        "Brand not found",
                        Constants.ErrorCode.Product.UPDATE_BRAND_NOT_FOUND
                ));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCpu(request.getCpu());
        product.setRam(request.getRam());
        product.setScreen(request.getScreen());
        product.setOperatingSystem(request.getOperatingSystem());
        product.setBatteryCapacity(request.getBatteryCapacity());
        product.setDesign(request.getDesign());
        product.setWarrantyInfo(request.getWarrantyInfo());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setBrand(brand);

        productRepository.save(product);

        List<ProductImage> existingImages = productImageRepository.findByProductId(product.getId());
        
        // Identify images to delete from Cloudinary (those in DB but not in request)
        List<String> publicIdsToDelete = existingImages.stream()
                .map(ProductImage::getPublicId)
                .filter(pid -> pid != null && !pid.isEmpty())
                .filter(pid -> request.getImages() == null || request.getImages().stream()
                        .noneMatch(imgReq -> pid.equals(imgReq.getPublicId())))
                .toList();

        productImageRepository.deleteByProductId(product.getId());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> productImages = request.getImages().stream().map(imgReq -> {
                ProductImage img = new ProductImage();
                img.setImageUrl(imgReq.getImageUrl());
                img.setIsThumbnail(imgReq.getIsThumbnail());
                img.setSortOrder(imgReq.getSortOrder());
                img.setPublicId(imgReq.getPublicId());
                img.setProduct(product);
                return img;
            }).toList();
            productImageRepository.saveAll(productImages);
            product.setImages(productImages);
        } else {
            product.setImages(List.of());
        }

        // Delete from Cloudinary after DB operations
        publicIdsToDelete.forEach(fileUploadService::deleteFile);

        return ApiResponseFactory.success(
                Constants.Message.Product.UPDATE_SUCCESS,
                mapToResponse(product)
        );
    }

    public BaseResultDTO<Void> delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        Constants.ErrorCode.Product.DELETE_NOT_FOUND
                ));

        List<ProductImage> images = productImageRepository.findByProductId(id);
        List<String> publicIds = images.stream()
                .map(ProductImage::getPublicId)
                .filter(pid -> pid != null && !pid.isEmpty())
                .toList();

        productImageRepository.deleteByProductId(id);
        productRepository.deleteById(id);

        // Delete from Cloudinary after DB operations
        publicIds.forEach(fileUploadService::deleteFile);

        return ApiResponseFactory.success(Constants.Message.Product.DELETE_SUCCESS);
    }

    private ProductResponse mapToResponse(Product p) {
        List<ProductImageResponse> images = p.getImages() == null
                ? List.of()
                : p.getImages().stream()
                .sorted(Comparator.comparing(
                        ProductImage::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(image -> ProductImageResponse.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .isThumbnail(image.getIsThumbnail())
                        .sortOrder(image.getSortOrder())
                        .publicId(image.getPublicId())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .cpu(p.getCpu())
                .ram(p.getRam())
                .screen(p.getScreen())
                .operatingSystem(p.getOperatingSystem())
                .batteryCapacity(p.getBatteryCapacity())
                .design(p.getDesign())
                .warrantyInfo(p.getWarrantyInfo())
                .description(p.getDescription())
                .soldQuantity(p.getSoldQuantity())
                .stockQuantity(p.getStockQuantity())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .images(images)
                .build();
    }
}
