package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.SpecDataType;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entity representing technical specifications of a Product.
 * Designed per Lucidchart Class Diagram:
 * - specKey: string
 * - dataType: SpecDataType (NUMBER, TEXT, BOOLEAN)
 * - numericValue: int/Integer (enables numeric filtering & recommendation matching)
 * - textValue: string
 * - unit: string (e.g. GB, inch, Hz)
 * - product: 1 Product to 0..N ProductSpecification
 */
@Entity
@Table(name = "product_specifications")
public class ProductSpecification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spec_key", nullable = false, length = 100)
    private String specKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private SpecDataType dataType = SpecDataType.TEXT;

    @Column(name = "numeric_value")
    private Integer numericValue;

    @Column(name = "text_value", length = 500)
    private String textValue;

    @Column(name = "unit", length = 50)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductSpecification() {
    }

    public ProductSpecification(String specKey, SpecDataType dataType, Integer numericValue, String textValue, String unit) {
        this.specKey = specKey;
        this.dataType = dataType;
        this.numericValue = numericValue;
        this.textValue = textValue;
        this.unit = unit;
    }

    // Convenience constructors
    public static ProductSpecification numberSpec(String key, int value, String unit) {
        return new ProductSpecification(key, SpecDataType.NUMBER, value, String.valueOf(value), unit);
    }

    public static ProductSpecification textSpec(String key, String text) {
        return new ProductSpecification(key, SpecDataType.TEXT, null, text, null);
    }

    public static ProductSpecification booleanSpec(String key, boolean boolVal) {
        return new ProductSpecification(key, SpecDataType.BOOLEAN, boolVal ? 1 : 0, String.valueOf(boolVal), null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecKey() {
        return specKey;
    }

    public void setSpecKey(String specKey) {
        this.specKey = specKey;
    }

    public SpecDataType getDataType() {
        return dataType;
    }

    public void setDataType(SpecDataType dataType) {
        this.dataType = dataType;
    }

    public Integer getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(Integer numericValue) {
        this.numericValue = numericValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
