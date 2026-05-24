package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.specifications.CustomerSpecification;
import com.project.ordermanagementsystem.core.specifications.OrderSpecification;
import com.project.ordermanagementsystem.core.specifications.ProductSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.model.Customer;
import com.project.ordermanagementsystem.model.Order;
import com.project.ordermanagementsystem.model.Product;
import com.project.ordermanagementsystem.repository.CustomerRepository;
import com.project.ordermanagementsystem.repository.OrderRepository;
import com.project.ordermanagementsystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;


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

        LOGGER.info("Stock Levels retrieved successfully with max quantity {}.", threshold);

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

    public KpiCardDTO getKpiCard(Integer threshold) {
        KpiCardDTO kpiCardDTO = new KpiCardDTO();
        kpiCardDTO.setProductKpiDTO(buildProductKpiDTO(threshold));
        kpiCardDTO.setOrderKpiDTO(buildOrderKpiDTO());
        kpiCardDTO.setCustomerKpiDTO(buildCustomerKpiDTO());
        LOGGER.info("Kpi Card retrieved successfully.");
        return kpiCardDTO;
    }

    private CustomerKpiDTO buildCustomerKpiDTO() {

        Specification<Customer> specActive = Specification.where(CustomerSpecification.isActive());
        Specification<Customer> specActiveByDate = specActive.and(CustomerSpecification.createdOn(LocalDate.now()));
        Specification<Customer> specActiveByYestersay = specActive.and(CustomerSpecification.createdOn(LocalDate.now().minusDays(1)));

        long count = customerRepository.count();
        long countToday = customerRepository.count(specActiveByDate);
        long countYesterday = customerRepository.count(specActiveByYestersay);

        long delta = countToday - countYesterday;

        return CustomerKpiDTO
                .builder()
                .totalCustomers(count)
                .deltaCustomersByYesterday(delta)
                .build();
    }

    private OrderKpiDTO buildOrderKpiDTO() {
        Specification<Order> specActive = Specification.where(OrderSpecification.isActive());
        Specification<Order> specActiveByDate = specActive.and(OrderSpecification.totalOrdersByDate(LocalDate.now()));
        Specification<Order> specActiveByYesterday = specActive.and(OrderSpecification.totalOrdersByDate(LocalDate.now().minusDays(1)));

        long countToday = orderRepository.count(specActiveByDate);
        long countYesterday = orderRepository.count(specActiveByYesterday);

        long delta = countToday - countYesterday;

        return OrderKpiDTO
                .builder()
                .totalOrdersByDate(countToday)
                .deltaOrdersByYesterday(delta)
                .build();
    }

    private ProductKpiDTO buildProductKpiDTO(Integer threshold) {
        Specification<Product> specActive = Specification.where(ProductSpecification.isActive());
        Specification<Product> specActiveByDate = specActive.and(ProductSpecification.totalProductsByDate(LocalDate.now()));
        Specification<Product> specActiveByYesterday = specActive.and(ProductSpecification.totalProductsByDate(LocalDate.now().minusDays(1)));

        Specification<Product> specActiveLowStockYesterday = specActive
                .and(ProductSpecification.stockLessThanOrEqual(threshold))
                .and(ProductSpecification.totalProductsByDate(LocalDate.now().minusDays(1)));

        Specification<Product> specActiveLowStockToday = specActive
                .and(ProductSpecification.stockLessThanOrEqual(threshold))
                .and(ProductSpecification.totalProductsByDate(LocalDate.now()));

        long count = productRepository.count(specActive);
        long countToday = productRepository.count(specActiveByDate);
        long countYesterday = productRepository.count(specActiveByYesterday);
        long lowStockYesterday = productRepository.count(specActiveLowStockYesterday);
        long lowStockToday = productRepository.count(specActiveLowStockToday);

        double deltaPct;
        if (countYesterday == 0 && countToday > 0) {
            deltaPct = 100.0; // 100% αύξηση αν χθες ήταν 0
        } else if (countYesterday == 0) {
            deltaPct = 0.0;
        } else {
            deltaPct = ((double)(countToday - countYesterday) / countYesterday) * 100;
        }
        long productDelta = lowStockToday - lowStockYesterday;

        return ProductKpiDTO.builder().totalProducts(count)
                .totalProductsByDate(countToday)
                .deltaPercentage(Math.round(deltaPct * 10.0) / 10.0)
                .productLowStock(lowStockToday)
                .deltaLowStockByYesterday(productDelta)
                .build();

    }

}
