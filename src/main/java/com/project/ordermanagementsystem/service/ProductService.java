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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final Mapper mapper;
    private final ProductRepository productRepository;

    @Transactional
    public ResponseDTO saveProduct(ProductInsertDTO productInsertDTO) {
        ResponseDTO responseDTO = new ResponseDTO();
        if(productRepository.existsByNameAndActiveTrue(productInsertDTO.getName())) {
            ErrorResponse errorResponse = new ErrorResponse("Product with name " + productInsertDTO.getName() + " already exists.");
            responseDTO.setErrorResponse(errorResponse);
            LOGGER.error(new AppObjectAlreadyExists("ProductName", "Product with name " + productInsertDTO.getName() + " already exists.").getMessage());
            return responseDTO;
        }

        Product product = mapper.mapToProductEntity(productInsertDTO);
        Product savedProduct = productRepository.save(product);
        responseDTO.setProductReadOnlyDTO(mapper.mapToProductReadOnlyDTO(savedProduct));
        LOGGER.info("Product with name: {} saved successfully.", savedProduct.getName());
        return responseDTO;
    }


    @Transactional
    public Page<ProductReadOnlyDTO> getPaginatedProducts(int page, int size, String sortBy, String sortDirection){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Specification<Product> spec = Specification.where(ProductSpecification.isActive());
        return productRepository.findAll(spec, pageable).map(mapper::mapToProductReadOnlyDTO);
    }

    public Page<ProductReadOnlyDTO> searchProducts(String name,
                                                   String sortBy,
                                                   String sortDirection,
                                                   int page,
                                                   int pageSize) {

        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Specification<Product> spec = Specification.where(
                ProductSpecification.trStringFieldLike("name", name)
        ).and(ProductSpecification.isActive());

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(mapper::mapToProductReadOnlyDTO);
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

            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            existingProduct = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("ProductNotFound",
                            String.format("Product with id: %s not found", dto.getId())));

            if (!existingProduct.getName().equals(dto.getName()) &&
                    productRepository.existsByNameAndActiveTrue(dto.getName())) {

                throw new AppObjectAlreadyExists("ProductName",
                        "Product with name " + dto.getName() + " already exists.");
            }

            existingProduct.setName(dto.getName());
            existingProduct.setDescription(dto.getDescription());
            existingProduct.setQuantity(dto.getQuantity());

            Product updatedProduct = productRepository.save(existingProduct);

            responseDTO.setProductReadOnlyDTO(mapper.mapToProductReadOnlyDTO(updatedProduct));

            LOGGER.info("Product with id: {} updated successfully.", updatedProduct.getId());

        } catch (AppObjectNotFound | AppObjectAlreadyExists e) {
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(new ErrorResponse(e.getMessage()));
        } catch (ValidationException e) {
            LOGGER.error(e.getMessage());
            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getBindingResult().getFieldError().getDefaultMessage()));
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO deleteProduct(ProductUpdateDTO dto){
        Product productToDelete;
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            productToDelete = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound("ProductNotFound",
                            String.format("Product with id: %s not found.", dto.getId())));
            if (!productToDelete.getItems().isEmpty()) {
                productToDelete.setActive(false);
                productRepository.save(productToDelete);
            } else {
                productRepository.delete(productToDelete);
            }
            ProductReadOnlyDTO returnedProduct = mapper.mapToProductReadOnlyDTO(productToDelete);
            responseDTO.setProductReadOnlyDTO(returnedProduct);
            LOGGER.info("Product with id: {} deleted successfully.", returnedProduct.getId());
        } catch (AppObjectNotFound e) {
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }

        return responseDTO;
    }
}
