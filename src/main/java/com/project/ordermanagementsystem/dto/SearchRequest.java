package com.project.ordermanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchRequest {
    private Integer page;
    private Integer pageSize;
    private String globalSearch;
    private List<FilterRequest> filters;
    private SortRequest sort;
}
