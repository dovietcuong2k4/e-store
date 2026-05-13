package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.ProductAiSearchRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.ProductImageResponse;
import com.example.eStore.dto.response.ProductResponse;
import com.example.eStore.entity.Product;
import com.example.eStore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductAiSearchService {
    private final ProductRepository productRepository;
    private final OpenRouterEmbeddingService openRouterEmbeddingService;

    @Transactional(readOnly = true)
    public BaseResultDTO<Page<ProductResponse>> search(ProductAiSearchRequest request) {
        Pageable pageable = PageRequest.of(Math.max(request.getPage(), 0), Math.max(request.getSize(), 1));
        String query = request.getQ() == null ? "" : request.getQ().trim();

        if (query.isBlank()) {
            return fallbackKeywordSearch(request, pageable, "Thiếu truy vấn, dùng keyword search");
        }

        try {
            List<Product> candidates = productRepository.findSemanticSearchCandidates(
                    request.getCategoryId(),
                    request.getBrandId(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getMinRating(),
                    request.isInStockOnly()
            );

            if (candidates.isEmpty()) {
                return fallbackKeywordSearch(request, pageable, "Không có sản phẩm phù hợp, dùng keyword search");
            }

            double[] queryVector = openRouterEmbeddingService.generateEmbedding(query);
            List<ScoredProduct> scoredProducts = new ArrayList<>();

            for (Product product : candidates) {
                double[] productVector = resolveProductVector(product);
                if (productVector.length == 0) {
                    continue;
                }

                double score = cosineSimilarity(queryVector, productVector);
                if (!Double.isFinite(score)) {
                    continue;
                }

                scoredProducts.add(new ScoredProduct(product, score));
            }

            if (scoredProducts.isEmpty()) {
                return fallbackKeywordSearch(request, pageable, "Không tính được semantic score, dùng keyword search");
            }

            scoredProducts.sort(Comparator
                    .comparingDouble(ScoredProduct::score)
                    .reversed()
                    .thenComparing(item -> item.product().getSoldQuantity() == null ? 0 : item.product().getSoldQuantity(), Comparator.reverseOrder()));

            List<ScoredProduct> pageItems = paginate(scoredProducts, pageable);
            List<ProductResponse> data = pageItems.stream()
                    .map(item -> mapToResponse(item.product(), item.score()))
                    .toList();

            Page<ProductResponse> page = new PageImpl<>(data, pageable, scoredProducts.size());
            return ApiResponseFactory.success("AI search completed", page);
        } catch (Exception exception) {
            log.warn("AI product search failed, falling back to keyword search: {}", exception.getMessage());
            return fallbackKeywordSearch(request, pageable, "AI search failed, keyword fallback used");
        }
    }

    private BaseResultDTO<Page<ProductResponse>> fallbackKeywordSearch(ProductAiSearchRequest request, Pageable pageable, String message) {
        List<Product> products = productRepository.searchByKeywordAndFilters(
                request.getQ(),
                request.getCategoryId(),
                request.getBrandId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinRating(),
                request.isInStockOnly(),
                pageable
        );

        List<ProductResponse> data = products.stream()
                .map(product -> mapToResponse(product, null))
                .toList();
        Page<ProductResponse> page = new PageImpl<>(data, pageable, data.size());
        return ApiResponseFactory.success(message, page);
    }

    private List<ScoredProduct> paginate(List<ScoredProduct> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= items.size()) {
            return List.of();
        }

        int end = Math.min(start + pageable.getPageSize(), items.size());
        return items.subList(start, end);
    }

    private double[] resolveProductVector(Product product) throws IOException, InterruptedException {
        if (product.getEmbeddingJson() == null || product.getEmbeddingJson().isBlank()) {
            return openRouterEmbeddingService.generateEmbedding(buildFallbackText(product));
        }
        return openRouterEmbeddingService.parseEmbeddingJson(product.getEmbeddingJson());
    }

    private String buildFallbackText(Product product) {
        StringBuilder builder = new StringBuilder();
        append(builder, product.getName());
        append(builder, product.getDescription());
        append(builder, product.getCategory() != null ? product.getCategory().getName() : null);
        append(builder, product.getBrand() != null ? product.getBrand().getName() : null);
        append(builder, product.getCpu());
        append(builder, product.getRam());
        append(builder, product.getScreen());
        append(builder, product.getOperatingSystem());
        append(builder, product.getBatteryCapacity());
        append(builder, product.getDesign());
        append(builder, product.getWarrantyInfo());
        if (product.getPrice() != null) {
            append(builder, priceRange(product.getPrice()));
        }
        return builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }

    private String priceRange(Long price) {
        long million = 1_000_000L;
        if (price < 5 * million) return "budget";
        if (price < 12 * million) return "midrange";
        if (price < 25 * million) return "upper-midrange";
        return "premium";
    }

    private double cosineSimilarity(double[] left, double[] right) {
        int size = Math.min(left.length, right.length);
        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;

        for (int i = 0; i < size; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }

        if (leftNorm == 0.0 || rightNorm == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private ProductResponse mapToResponse(Product product, Double semanticScore) {
        List<ProductImageResponse> images = product.getImages() == null
                ? List.of()
                : product.getImages().stream()
                .sorted(Comparator.comparing(image -> image.getSortOrder(), Comparator.nullsLast(Integer::compareTo)))
                .map(image -> ProductImageResponse.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .isThumbnail(image.getIsThumbnail())
                        .sortOrder(image.getSortOrder())
                        .publicId(image.getPublicId())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .cpu(product.getCpu())
                .ram(product.getRam())
                .screen(product.getScreen())
                .operatingSystem(product.getOperatingSystem())
                .batteryCapacity(product.getBatteryCapacity())
                .design(product.getDesign())
                .warrantyInfo(product.getWarrantyInfo())
                .description(product.getDescription())
                .semanticScore(semanticScore)
                .soldQuantity(product.getSoldQuantity())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .images(images)
                .build();
    }

    private record ScoredProduct(Product product, double score) {
    }
}