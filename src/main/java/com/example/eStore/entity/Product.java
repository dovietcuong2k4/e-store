package com.example.eStore.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long price;
    private String cpu;
    private String ram;
    private String screen;
    private String operatingSystem;
    private String batteryCapacity;
    private String design;
    private String warrantyInfo;
    private String description;
    private Integer soldQuantity;
    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference("category-product")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    @JsonBackReference("brand-product")
    private Brand brand;

    @OneToMany(mappedBy = "product")
    @JsonManagedReference
    private List<ProductImage> images;
}
