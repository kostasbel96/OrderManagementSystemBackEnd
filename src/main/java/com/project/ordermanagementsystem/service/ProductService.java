package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.ProductSpecification;
import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.Product;
import com.project.ordermanagementsystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final Mapper mapper;
    private final ProductRepository productRepository;

    @Transactional
    public ProductReadOnlyDTO saveProduct(ProductInsertDTO productInsertDTO) throws AppObjectAlreadyExists {
        if(productRepository.findByName(productInsertDTO.getName()).isPresent()) {
            throw new AppObjectAlreadyExists("ProductName", "Product with name " + productInsertDTO.getName() + " already exists.");
        }

        Product product = mapper.mapToProductEntity(productInsertDTO);
        Product savedProduct = productRepository.save(product);
        return mapper.mapToProductReadOnlyDTO(savedProduct);
    }


    @Transactional
    public Page<ProductReadOnlyDTO> getPaginatedProducts(int page, int size){
        String defaultSort = "id";

        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());

        return productRepository.findAll(pageable).map(mapper::mapToProductReadOnlyDTO);
    }

    public List<ProductReadOnlyDTO> searchProducts(String name){

        Specification<Product> spec = Specification.where(
                ProductSpecification.trStringFieldLike("name", name)
        );

        List<Product> products = productRepository.findAll(spec);

        return products.stream().map(mapper::mapToProductReadOnlyDTO).toList();

    }

    public ResponseDTO getProductByName(String name) {
        Product product;
        ResponseDTO responseDTO = new ResponseDTO();
        try{
            product = productRepository.findByName(name)
                    .orElseThrow(() ->
                            new AppObjectNotFound("ProductNotFound", String.format("Product with name: %s not found", name))
                    );
            responseDTO.setProductReadOnlyDTO(mapper.mapToProductReadOnlyDTO(product));
            LOGGER.info("Product with name: {} found successfully.", product.getName());
        } catch(AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;
    }

    @Transactional
    public ResponseDTO updateProduct(ProductUpdateDTO dto, BindingResult bindingResult) {
        Product existingProduct;
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            existingProduct = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("ProductNotFound", String.format("Product with id: %s not found", dto.getId())));

            existingProduct.setName(dto.getName());
            existingProduct.setDescription(dto.getDescription());
            existingProduct.setQuantity(dto.getQuantity());

            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            Product updatedProduct = productRepository.save(existingProduct);
            responseDTO.setProductReadOnlyDTO(mapper.mapToProductReadOnlyDTO(updatedProduct));
            LOGGER.info("Product with id: {} updated successfully.", updatedProduct.getId());
        } catch (AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        } catch (ValidationException e){
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(new ErrorResponse(e.getBindingResult().getFieldError().getDefaultMessage()));
        }

        return responseDTO;

    }
}
