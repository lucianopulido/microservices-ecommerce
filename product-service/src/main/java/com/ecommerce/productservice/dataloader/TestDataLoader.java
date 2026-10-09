package com.ecommerce.productservice.dataloader;

import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        Product product = Product.builder()
                .name("Samsung Galaxy S24")
                .description("Smartphone con IA")
                .price(BigDecimal.valueOf(1200))
                .build();

        this.productRepository.findByName(product.getName()).ifPresentOrElse(
                existingProduct -> System.out.println("Producto ya existe: " + existingProduct.getName()),
                () -> {
                    this.productRepository.save(product);
                    System.out.println("Datos de prueba cargados: " + product.getName());
                }
        );
    }
}
