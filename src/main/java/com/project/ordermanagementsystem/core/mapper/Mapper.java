package com.project.ordermanagementsystem.core.mapper;

import com.project.ordermanagementsystem.dto.ProductInsertDTO;
import com.project.ordermanagementsystem.dto.ProductReadOnlyDTO;
import com.project.ordermanagementsystem.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mapper {

    public Product mapToProductEntity(ProductInsertDTO dto){
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setQuantity(dto.getQuantity());

        return product;
    }

    public ProductReadOnlyDTO mapToProductReadOnlyDTO(Product product){
        ProductReadOnlyDTO dto = new ProductReadOnlyDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setQuantity(product.getQuantity());

        return dto;
    }

}
