package com.example.crm_system.controller;

import com.example.crm_system.entity.ProductEntity;
import com.example.crm_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")

public class ProductController {

    @Autowired
    private ProductService productService;

    // Create product for a business
    @PostMapping("/business/{businessId}")
    public ResponseEntity<ProductEntity> addProduct(
            @PathVariable Long businessId,
            @RequestBody ProductEntity product) {
        return ResponseEntity.ok(productService.addProduct(businessId, product));
    }

    // Get all products by business
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<ProductEntity>> getProductsByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(productService.getProductsByBusiness(businessId));
    }

    // Get single product by id
    @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<ProductEntity> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductEntity product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    // Update usage for a product
@PutMapping("/{id}/usage")
public ResponseEntity<ProductEntity> updateUsage(
        @PathVariable Long id,
        @RequestParam Integer usageQuantity) {
    return ResponseEntity.ok(productService.updateUsage(id, usageQuantity));
}

}
