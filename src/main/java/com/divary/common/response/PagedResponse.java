package com.divary.common.response;

import com.divary.common.dto.BasePaginationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.divary.common.dto.BasePaginationDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    
    private List<T> content;
    private BasePaginationDto pagination;
    
    public static <T> PagedResponse<T> of(List<T> content, BasePaginationDto pagination) {
        return PagedResponse.<T>builder()
                .content(content)
                .pagination(pagination)
                .build();
    }
    
    public static <T> PagedResponse<T> of(List<T> content, int limit, int currentPage, int totalPage) {
        return PagedResponse.<T>builder()
                .content(content)
                .pagination(BasePaginationDto.of(limit, currentPage, totalPage))
                .build();
    }
}