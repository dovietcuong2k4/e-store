package com.example.eStore.service;

import com.example.eStore.dto.request.ProductRequest;
import com.example.eStore.dto.response.ProductResponse;
import com.example.eStore.entity.Brand;
import com.example.eStore.entity.Category;
import com.example.eStore.entity.Product;
import com.example.eStore.repository.BrandRepository;
import com.example.eStore.repository.CategoryRepository;
import com.example.eStore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductResponse create(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow();

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow();

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setBrand(brand);

        productRepository.save(product);

        return mapToResponse(product);
    }

    public Page<ProductResponse> getAll(String keyword, Pageable pageable) {

        Page<Product> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = productRepository
                    .findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    public ProductResponse update(Long id, ProductRequest request) {

        Product product = productRepository.findById(id).orElseThrow();

        product.setName(request.getName());
        product.setPrice(request.getPrice());

        productRepository.save(product);

        return mapToResponse(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .categoryName(p.getCategory().getName())
                .brandName(p.getBrand().getName())
                .build();
    }
}
