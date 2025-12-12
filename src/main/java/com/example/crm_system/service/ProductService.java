package com.example.crm_system.service;

import com.example.crm_system.entity.Business;
import com.example.crm_system.entity.ProductEntity;
import com.example.crm_system.repository.BusinessRepository;
import com.example.crm_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BusinessRepository businessRepository;

    // Create product under a business
    public ProductEntity addProduct(Long businessId, ProductEntity product) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        product.setBusiness(business);
        return productRepository.save(product);
    }

    // Get all products of a business
    public List<ProductEntity> getProductsByBusiness(Long businessId) {
        return productRepository.findByBusinessId(businessId);
    }

    // Get single product by id
    public Optional<ProductEntity> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Update product details
    public ProductEntity updateProduct(Long productId, ProductEntity updatedProduct) {
        ProductEntity existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existing.setProductName(updatedProduct.getProductName());
        existing.setBrand(updatedProduct.getBrand());
        existing.setSingleUnitSize(updatedProduct.getSingleUnitSize());
        existing.setProductDescription(updatedProduct.getProductDescription());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCode(updatedProduct.getCode());
        existing.setType(updatedProduct.getType());
        existing.setGST(updatedProduct.getGST());
        existing.setUnitSellingPrice(updatedProduct.getUnitSellingPrice());
        existing.setMinStockQuantity(updatedProduct.getMinStockQuantity());
        existing.setLowstockalert(updatedProduct.getLowstockalert());
        existing.setInternalConsumptionOnly(updatedProduct.getInternalConsumptionOnly());
        existing.setSupplier(updatedProduct.getSupplier());
        existing.setInvoiceNumber(updatedProduct.getInvoiceNumber());
        existing.setTotalBillAmount(updatedProduct.getTotalBillAmount());

        return productRepository.save(existing);
    }

    // ✅ Update product usage (reduce stock)
    public ProductEntity updateUsage(Long productId, Integer usageQuantity) {
    ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

    if (usageQuantity == null || usageQuantity <= 0) {
        throw new RuntimeException("Usage quantity must be greater than 0");
    }

    if (product.getQuantity() == null || product.getQuantity() < usageQuantity) {
        throw new RuntimeException("Not enough stock available");
    }

    // Safely handle null usage
    Integer currentUsage = product.getUsage();
    if (currentUsage == null) {
        currentUsage = 0;
    }

    // Increase usage
    product.setUsage(currentUsage + usageQuantity);

    // Decrease stock
    product.setQuantity(product.getQuantity() - usageQuantity);

    // updated timestamp is handled automatically by @UpdateTimestamp
    return productRepository.save(product);
}
    // Delete product
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}
