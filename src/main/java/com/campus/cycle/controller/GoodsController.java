package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.ApplyBuyDTO;
import com.campus.cycle.dto.GoodsIdDTO;
import com.campus.cycle.dto.PublishGoodsDTO;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.service.GoodsService;
import com.campus.cycle.service.UploadService;
import com.campus.cycle.vo.GoodsDetailVO;
import com.campus.cycle.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 商品接口
 */
@Slf4j
@Tag(name = "商品")
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final UploadService uploadService;

    @Operation(summary = "上传商品图片（multipart，返回可访问 URL）")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        log.info("==== upload 请求到达, file={}, size={}, host={} ====",
                file == null ? null : file.getOriginalFilename(),
                file == null ? 0 : file.getSize(),
                request.getHeader("Host"));
        return Result.success(uploadService.saveImage(file, request));
    }

    @Operation(summary = "本校商品列表（分页；schoolId 不传，从登录用户绑定学校取；已售出置灰展示、不隐藏）")
    @GetMapping("/list")
    public Result<PageResult<Goods>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false, defaultValue = "1") long pageNum,
                                          @RequestParam(required = false, defaultValue = "0") long pageSize) {
        // pageSize 传 0/缺省时使用 Nacos 中的 campus.goods.page-size
        return Result.success(goodsService.list(keyword, category, pageNum, pageSize));
    }

    @Operation(summary = "商品详情（含卖家信息与评价）")
    @GetMapping("/detail/{id}")
    public Result<GoodsDetailVO> detail(@PathVariable String id) {
        return Result.success(goodsService.detail(id));
    }

    @Operation(summary = "我发布的商品")
    @GetMapping("/mine")
    public Result<List<Goods>> mine() {
        return Result.success(goodsService.mine());
    }

    @Operation(summary = "发布商品（卖家有未结清手续费时禁止）")
    @PostMapping("/publish")
    public Result<Goods> publish(@Valid @RequestBody PublishGoodsDTO dto) {
        return Result.success(goodsService.publish(dto));
    }

    @Operation(summary = "买家提交购买申请（创建订单：待卖家确认）")
    @PostMapping("/apply")
    public Result<String> applyBuy(@Valid @RequestBody ApplyBuyDTO dto) {
        return Result.success(goodsService.applyBuy(dto));
    }

    @Operation(summary = "买家发起站内沟通（不存在则创建会话）")
    @PostMapping("/conversation/start")
    public Result<String> startConversation(@RequestBody ApplyBuyDTO dto) {
        return Result.success(goodsService.startConversation(dto.getGoodsId()));
    }

    @Operation(summary = "卖家下架商品（仅自己在售商品可下架，下架后买家不可见）")
    @PostMapping("/off-shelf")
    public Result<Void> offShelf(@Valid @RequestBody GoodsIdDTO dto) {
        goodsService.offShelf(dto.getGoodsId());
        return Result.success();
    }

    @Operation(summary = "卖家重新上架（已下架商品恢复在售）")
    @PostMapping("/on-shelf")
    public Result<Void> onShelf(@Valid @RequestBody GoodsIdDTO dto) {
        goodsService.onShelf(dto.getGoodsId());
        return Result.success();
    }

    @Operation(summary = "卖家删除商品（未售出的商品才可删除，逻辑删除）")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody GoodsIdDTO dto) {
        goodsService.delete(dto.getGoodsId());
        return Result.success();
    }
}
