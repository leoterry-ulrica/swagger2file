package com.dist.xdata.swagger2file.utils;

import cn.afterturn.easypoi.word.WordExportUtil;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportWordUtils {
    /**
     * 导出word
     * <p>第一步生成替换后的word文件，只支持docx</p>
     * <p>第二步下载生成的文件</p>
     * <p>第三步删除生成的临时文件</p>
     * 模版变量中变量格式：{{foo}}
     *
     * @param templatePath word模板地址
     * @param temDir       生成临时文件存放地址
     * @param fileName     文件名
     * @param params       替换的参数
     */
    public static void exportWord(String templatePath, String temDir, String fileName, List<Map<String, Object>> params) throws Exception {
        Assert.notNull(templatePath, "模板路径不能为空");
        Assert.notNull(temDir, "临时文件路径不能为空");
        Assert.notNull(fileName, "导出文件名不能为空");
        Assert.isTrue(fileName.endsWith(".docx"), "word导出请使用docx格式");
        if (!temDir.endsWith("/")) {
            temDir = temDir + File.separator;
        }
        File dir = new File(temDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        XWPFDocument doc = WordExportUtil.exportWord07(templatePath, params);
        String tmpPath = temDir + fileName;
        FileOutputStream fos = new FileOutputStream(tmpPath);
        doc.write(fos);
    }

    /**
     * 删除零时生成的文件
     */
    public static void delFileWord(String filePath, String fileName) {
        File file = new File(filePath + fileName);
        File file1 = new File(filePath);
        file.delete();
        file1.delete();
    }

    public static void main(String[] args) throws Exception {
        List<Map<String, Object>> dataMaps = new ArrayList<>();

        Map<String, Object> parameter;
        for (int i = 0; i < 5; i++) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMaps.add(dataMap);
            dataMap.put("controllerName", "controller-" + i);
            dataMap.put("interfaceName", "interface-" + i);
            dataMap.put("interfaceDesc", "interfacedesc-" + i);
            dataMap.put("url", "url-" + i);
            dataMap.put("method", "method-" + i);
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                parameter = new HashMap<>();
                parameter.put("name", "para-name-" + j);
                parameter.put("dataType", "datatype-" + j);
                parameter.put("paraType", "paratype-" + j);
                parameter.put("required", "Y" + j);
                parameter.put("description", "desc-" + j);
                parameters.add(parameter);
            }
            dataMap.put("ps", parameters);
            dataMap.put("responseType", "array");
            dataMap.put("responseDesc", "xxx123");
        }

        XWPFDocument doc = WordExportUtil.exportWord07("C:\\Users\\75423\\Desktop\\swagger-word-template.docx", dataMaps);
        String tmpPath = "C:\\Users\\75423\\Desktop\\output.docx";
        FileOutputStream fos = new FileOutputStream(tmpPath);
        doc.write(fos);
    }
}