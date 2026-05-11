package com.petshop.repository;

import com.petshop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p JOIN FETCH p.category c
            WHERE (:categoryId IS NULL OR c.id = :categoryId)
              AND (:search IS NULL OR LOWER(p.name) LIKE :searchPattern OR LOWER(p.description) LIKE :searchPattern)
              AND (:available IS NULL OR p.available = :available)
            """)
    Page<Product> findByFilters(
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            @Param("searchPattern") String searchPattern,
            @Param("available") Boolean available,
            Pageable pageable);

    List<Product> findTop8ByAvailableTrueOrderByCreatedAtDesc();
}
