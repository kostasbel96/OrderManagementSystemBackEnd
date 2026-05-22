package com.project.ordermanagementsystem.rest;

import com.project.ordermanagementsystem.dto.StockLevelDTO;
import com.project.ordermanagementsystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardRestController {

    private final DashboardService dashboardService;

    @GetMapping("dashboard/stockLevels/{threshold}")
    public ResponseEntity<List<StockLevelDTO>> getStockLevels(@PathVariable Integer threshold) {
        return new ResponseEntity<>(dashboardService.getStockLevels(threshold), HttpStatus.OK);
    }

}
