package com.example.eStore.repository;

import com.example.eStore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(Long id);

    @Query("SELECT p FROM Product p WHERE "
            + "(:keyword IS NULL OR ("
            + "LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.cpu, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.ram, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.screen, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.operatingSystem, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.design, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.category.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(p.brand.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))"
            + ")) "
            + "AND (:category IS NULL OR LOWER(p.category.name) = LOWER(:category)) "
            + "AND (:brand IS NULL OR LOWER(COALESCE(p.brand.name, '')) LIKE LOWER(CONCAT('%', :brand, '%'))) "
            + "AND (:minPrice IS NULL OR p.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> search(@Param("keyword") String keyword,
                         @Param("category") String category,
                         @Param("brand") String brand,
                         @Param("minPrice") Long minPrice,
                         @Param("maxPrice") Long maxPrice,
                         Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.brand
            LEFT JOIN FETCH p.images
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:brandId IS NULL OR p.brand.id = :brandId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStockOnly = false OR COALESCE(p.stockQuantity, 0) > 0)
              AND (:minRating IS NULL OR COALESCE((SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product = p), 0) >= :minRating)
            """)
    List<Product> findSemanticSearchCandidates(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("minRating") Double minRating,
            @Param("inStockOnly") boolean inStockOnly);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.brand
            LEFT JOIN FETCH p.images
            WHERE (:keyword IS NULL OR :keyword = '' OR (
                LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.cpu, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.ram, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.screen, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.operatingSystem, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.design, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.category.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(p.brand.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:brandId IS NULL OR p.brand.id = :brandId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStockOnly = false OR COALESCE(p.stockQuantity, 0) > 0)
              AND (:minRating IS NULL OR COALESCE((SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product = p), 0) >= :minRating)
            """)
    List<Product> searchByKeywordAndFilters(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("minRating") Double minRating,
            @Param("inStockOnly") boolean inStockOnly,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE "
            + "p.category.id = :categoryId AND p.brand.id = :brandId AND p.id != :currentProductId")
    List<Product> findRelatedProducts(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("currentProductId") Long currentProductId,
            Pageable pageable);
}
