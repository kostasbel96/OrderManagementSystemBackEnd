package com.project.ordermanagementsystem.service;

import com.project.ordermanagementsystem.core.enums.OrderStatus;
import com.project.ordermanagementsystem.core.enums.RouteStatus;
import com.project.ordermanagementsystem.core.exceptions.AppObjectAlreadyExists;
import com.project.ordermanagementsystem.core.exceptions.AppObjectNotFound;
import com.project.ordermanagementsystem.core.exceptions.ValidationException;
import com.project.ordermanagementsystem.core.specifications.RouteSpecification;
import com.project.ordermanagementsystem.dto.*;
import com.project.ordermanagementsystem.mapper.Mapper;
import com.project.ordermanagementsystem.model.*;
import com.project.ordermanagementsystem.repository.DriverRepository;
import com.project.ordermanagementsystem.repository.OrderRepository;
import com.project.ordermanagementsystem.repository.RouteRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteService.class);
    private final Mapper mapper;
    private final OrderRepository orderRepository;
    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public Page<RouteReadOnlyDTO> getPaginatedRoutes(
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );

        Specification<Route> spec = Specification.where(RouteSpecification.isActive());

        return routeRepository.findAll(spec, pageable)
                .map(mapper::mapToRouteReadOnlyDTO);
    }

    @Transactional(readOnly = true)
    public Page<RouteReadOnlyDTO> searchRoutes(SearchRequest request) {

        Sort.Direction direction = Sort.Direction.fromString(
                request.getSort().getSort()
        );

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getPageSize(),
                Sort.by(direction, request.getSort().getField())
        );

        Specification<Route> spec = Specification.where(RouteSpecification.isActive());

        // global search (name / notes / driver etc.)
        if (request.getGlobalSearch() != null && !request.getGlobalSearch().isBlank()) {
            spec = spec.and(RouteSpecification.globalSearch(request.getGlobalSearch()));
        }

        // dynamic filters
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (FilterRequest filter : request.getFilters()) {
                spec = spec.and(RouteSpecification.fromFilter(filter));
            }
        }

        return routeRepository.findAll(spec, pageable)
                .map(mapper::mapToRouteReadOnlyDTO);
    }

    @Transactional
    public ResponseDTO saveRoute(RouteInsertDTO dto, BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {
            List<Long> orderIdsExistInRoute = new ArrayList<>();
            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            DriverPerson driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "DriverNotFound",
                            "Driver not found"
                    ));
            List<Long> orderIds = dto.getOrderIds();
            for (Long orderId : orderIds) {
                Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new AppObjectNotFound("OrderNotFound", "Order not found"));

                if (order.getRoute() != null) {
                    orderIdsExistInRoute.add(orderId);
                }
            }

            if (!orderIdsExistInRoute.isEmpty()){
                throw new AppObjectAlreadyExists(
                        "OrderAlreadyExists",
                        "Order(s) with id(s): " +  orderIdsExistInRoute.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", ")) + " already assigned to a route"
                );
            }

            Route route = new Route();
            route.setName(dto.getName());
            route.setNotes(dto.getNotes());
            route.setDate(dto.getDate());
            route.setDriver(driver);
            route.setStatus(RouteStatus.PLANNED);

            Route savedRoute = routeRepository.save(route);

            for (Long orderId : orderIds) {

                Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new AppObjectNotFound(
                                "OrderNotFound",
                                "Order not found"
                        ));
                order.setStatus(OrderStatus.ASSIGNED);
                savedRoute.addOrder(order);
                orderRepository.save(order);
            }

            LOGGER.info("Route with id {} saved successfully", savedRoute.getId());

            RouteReadOnlyDTO routeReadOnlyDTO = mapper.mapToRouteReadOnlyDTO(savedRoute);
            responseDTO.setRouteReadOnlyDTO(routeReadOnlyDTO);
        } catch (AppObjectNotFound | AppObjectAlreadyExists | ValidationException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
            LOGGER.error(e.getMessage());
        }

        return responseDTO;

    }

    @Transactional
    public ResponseDTO updateRoute(RouteUpdateDTO dto, BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {

            if (bindingResult.hasErrors()) {
                throw new ValidationException(bindingResult);
            }

            Route existingRoute = routeRepository.findById(dto.getId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "RouteNotFound",
                            "Route with id: " + dto.getId() + " not found"
                    ));

            // update basic fields
            existingRoute.setName(dto.getName());
            existingRoute.setNotes(dto.getNotes());
            existingRoute.setStatus(dto.getStatus());
            existingRoute.setDate(dto.getDate());
            // driver update
            DriverPerson driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new AppObjectNotFound(
                            "DriverNotFound",
                            "Driver not found"
                    ));

            existingRoute.setDriver(driver);

            // store old orders (optional safety / reassign logic)
            existingRoute.getOrders().clear();

            // rebuild orders
            if (dto.getOrderIds() != null && !dto.getOrderIds().isEmpty()) {

                for (Long orderId : dto.getOrderIds()) {

                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new AppObjectNotFound(
                                    "OrderNotFound",
                                    "Order not found"
                            ));

                    if (order.getRoute() != null && !order.getRoute().getId().equals(existingRoute.getId())) {
                        throw new AppObjectAlreadyExists(
                                "OrderAlreadyAssigned",
                                "Order with id: " + orderId + " already belongs to route with id: " + order.getRoute().getId()
                        );
                    }

                    existingRoute.addOrder(order);
                    orderRepository.save(order);
                }
            }

            Route updatedRoute = routeRepository.save(existingRoute);

            responseDTO.setRouteReadOnlyDTO(
                    mapper.mapToRouteReadOnlyDTO(updatedRoute)
            );

            LOGGER.info("Route with id {} updated successfully", updatedRoute.getId());

        } catch (AppObjectNotFound | ValidationException | AppObjectAlreadyExists e) {

            LOGGER.error(e.getMessage());

            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }

    @Transactional
    public ResponseDTO getRouteById(Long id) {
        Route route;
        ResponseDTO responseDTO = new ResponseDTO();
        try{
            route = routeRepository.findById(id)
                    .orElseThrow(()-> new AppObjectNotFound("RouteNotFound",String.format("Route with id: %s not found", id)));
            LOGGER.info("Route with id: {} found successfully.", route.getId());
            responseDTO.setRouteReadOnlyDTO(mapper.mapToRouteReadOnlyDTO(route));
        } catch (AppObjectNotFound e){
            LOGGER.error(e.getMessage());
            ErrorResponse errorResponse =
                    new ErrorResponse(e.getMessage());
            responseDTO.setErrorResponse(errorResponse);
        }
        return responseDTO;

    }

    @Transactional
    public ResponseDTO deleteRoute(Long id) {

        ResponseDTO responseDTO = new ResponseDTO();

        try {

            Route route = routeRepository.findById(id)
                    .orElseThrow(() -> new AppObjectNotFound(
                            "RouteNotFound",
                            "Route with id: " + id + " not found"
                    ));

            if (!route.isActive()) {
                throw new IllegalStateException("Route already deleted");
            }

            // optional: handle assigned orders
            for (Order order : route.getOrders()) {

                // unassign from route
                order.setRoute(null);

                // business rule: revert status if needed
                if (order.getStatus() == OrderStatus.ASSIGNED) {
                    order.setStatus(OrderStatus.PENDING);
                }
            }

            // SOFT DELETE
            route.setActive(false);

            Route saved = routeRepository.save(route);

            responseDTO.setRouteReadOnlyDTO(
                    mapper.mapToRouteReadOnlyDTO(saved)
            );

            LOGGER.info("Route with id {} cancelled successfully", saved.getId());

        } catch (AppObjectNotFound | IllegalStateException e) {

            LOGGER.error(e.getMessage());

            responseDTO.setErrorResponse(
                    new ErrorResponse(e.getMessage())
            );
        }

        return responseDTO;
    }
}
