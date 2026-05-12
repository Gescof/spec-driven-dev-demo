package com.petshop.dto;

import java.util.List;

public record ProductPageResponse(List<ProductSummaryResponse> content, long totalElements,
                                  int totalPages, int page, int size) {}
