package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.specifications.ProductSpecification;
import com.project.ordermanagementsystem.dto.StockLevelDTO;
import com.project.ordermanagementsystem.model.Product;
import com.project.ordermanagementsystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);


    public List<StockLevelDTO> getStockLevels(Integer threshold) {

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());
        spec = spec.and(ProductSpecification.stockLessThanOrEqual(threshold));

        List<Product> products = productRepository.findAll(
                spec,
                Sort.by("quantity").ascending()
        );

        int maxQuantity = products.stream()
                .mapToInt(Product::getQuantity)
                .max()
                .orElse(1);

        return products.stream()
                .map(product -> {

                    int pct = (int) (
                            (product.getQuantity() / (double) maxQuantity) * 100
                    );

                    return new StockLevelDTO(
                            product.getName(),
                            product.getQuantity(),
                            pct
                    );
                })
                .toList();
    }



}
