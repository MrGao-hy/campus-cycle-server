package com.campus.cycle.controller;

import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.Result;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.dto.ApplyBuyDTO;
import com.campus.cycle.dto.PublishGoodsDTO;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.service.GoodsService;
import com.campus.cycle.vo.GoodsDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES = Set.of("jpg", "jpeg", "png", "webp", "gif");

    /** 上传目录（application.yml: campus.upload.dir） */
    @Value("${campus.upload.dir:./uploads}")
    private String uploadDir;

    @Operation(summary = "上传商品图片（multipart，返回可访问 URL）")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        log.info("==== upload 请求到达, file={}, size={}, host={} ====",
                file == null ? null : file.getOriginalFilename(),
                file == null ? 0 : file.getSize(),
                request.getHeader("Host"));
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/webp/gif 图片");
        }
        // 按日期分目录，避免单目录文件过多
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(uploadDir, datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(dir, filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("图片保存失败，请重试");
        }
        // 基于请求 Host 动态拼 URL：H5/开发者工具/真机预览均可直接访问
        String host = request.getHeader("Host");
        String url = (host != null ? "http://" + host : "") + "/uploads/" + datePath + "/" + filename;
        return Result.success(url);
    }

    @Operation(summary = "本校商品列表（已售出置灰展示、不隐藏）")
    @GetMapping("/list")
    public Result<List<Goods>> list(@RequestParam String schoolId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String category) {
        return Result.success(goodsService.list(schoolId, keyword, category));
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
}
