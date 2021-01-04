package com.dist.xdata.swagger2file.controller;

import com.dist.xdata.swagger2file.service.WordService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class FileController {

    @Autowired
    private WordService wordService;

    @GetMapping("exp/doc")
    @ApiOperation("导出格式为word的文档。")
    public ResponseEntity exportDoc(
            @RequestParam
            @ApiParam("swagger api json访问地址")
            String swaggerUrl
    ) throws Exception {
        String filePath = this.wordService.exportDoc(swaggerUrl);
        if (StringUtils.isEmpty(filePath)) {
            throw new Exception("导出文件失败");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        // chrome浏览器下载文件可能出现：ERR_RESPONSE_HEADERS_MULTIPLE_CONTENT_DISPOSITION，
        // 产生原因：可能是因为文件名中带有英文半角逗号,
        // 解决办法：确保 filename 参数使用双引号包裹[1]
        headers.add("Content-Disposition", "attachment; filename=\"swagger2doc.docx\"");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(filePath));
    }
}
