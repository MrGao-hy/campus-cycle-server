package com.campus.cycle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页结果（对齐前端触底加载所需的 hasMore）
 */
@Data
@Schema(name = "PageResult", description = "分页结果")
public class PageResult<T> {

    @Schema(description = "当前页数据")
    private List<T> list;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "当前页码（从 1 开始）")
    private long pageNum;

    @Schema(description = "每页条数")
    private long pageSize;

    @Schema(description = "总页数")
    private long pages;

    @Schema(description = "是否还有下一页（前端据此决定是否继续触底加载）")
    private boolean hasMore;

    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list == null ? Collections.emptyList() : list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize);
        result.setHasMore(pageNum < result.getPages());
        return result;
    }

    /** 空页（无数据时返回，hasMore=false） */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return PageResult.of(Collections.emptyList(), 0, pageNum, pageSize);
    }
}
