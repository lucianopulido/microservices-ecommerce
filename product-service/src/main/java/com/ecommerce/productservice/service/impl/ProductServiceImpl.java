package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.dto.ProductRequestDTO;
import com.ecommerce.productservice.dto.ProductResponseDTO;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDto) {
        Product product = this.productMapper.toProduct(productRequestDto);
        Product createdProduct = this.productRepository.save(product);
        log.info("Product created: {}", createdProduct.getName());
        return this.productMapper.toProductResponseDto(createdProduct);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return this.productRepository.findAll()
                .stream()
                .map(this.productMapper::toProductResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return this.productMapper.toProductResponseDto(product);
    }

    @Override
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO productRequestDto) {
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        this.productMapper.updateProductFromRequest(productRequestDto, product);
        Product updatedProduct = this.productRepository.save(product);
        log.info("Product updated: {}", updatedProduct.getName());
        return this.productMapper.toProductResponseDto(updatedProduct);
    }

    @Override
    public void deleteProduct(String id) {
        if (!this.productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        this.productRepository.deleteById(id);
        log.info("Product deleted with id: {}", id);
    }
}
