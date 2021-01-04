package com.dist.xdata.swagger2file.service;

import cn.afterturn.easypoi.word.WordExportUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dist.xdata.swagger2file.model.*;
import com.dist.xdata.swagger2file.utils.ExportWordUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WordService {

    /**
     * 模板里面的基本信息行数（根据自己的模板进行修改）
     */
    private static int TEMPLATE_BASIC_INFO_ROW_COUNT = 6;
    /**
     * 模板里面合并起始列索引号（根据自己的模板进行修改），如果是-1，则不进行合并。
     */
    private static int TEMPLATE_MERGE_FORM_COLUMN = 2;
    /**
     * 模板里面合并终止列索引号（根据自己的模板进行修改），如果是-1，则不进行合并。
     */
    private static int TEMPLATE_MERGE_TO_COLUMN = 4;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 导出word文档
     *
     * @param swaggerUrl swagger api json地址
     * @return
     */
    public String exportDoc(String swaggerUrl) throws Exception {
        SwaggerAPIDTO swaggerAPIDTO = this.restTemplate.getForObject(swaggerUrl, SwaggerAPIDTO.class);
        // 上下文ip和端口
        String serverUrl = swaggerAPIDTO.getServers().get(0).getUrl();

        List<Map<String, Object>> dataMaps = new ArrayList<>();
        Map<String, Object> parameter;
        int interfaceCount = swaggerAPIDTO.getPaths().size();
        if (0 == interfaceCount) {
            return "";
        }
        // 请求参数行数，默认两行
        int REQUEST_PARAMETER_ROW_COUNT = 2;
        for(Map.Entry<String, Map<String, MethodDTO>> pathEntry : swaggerAPIDTO.getPaths().entrySet()) {
            for (Map.Entry<String, MethodDTO> methodEntry : pathEntry.getValue().entrySet()) {
                MethodDTO method = methodEntry.getValue();
                Map<String, Object> dataMap = new HashMap<>();
                dataMaps.add(dataMap);
                dataMap.put("controllerName", method.getTags().get(0));
                dataMap.put("interfaceName", pathEntry.getKey());
                dataMap.put("interfaceDesc", method.getSummary());
                dataMap.put("url", serverUrl + pathEntry.getKey());
                dataMap.put("method", methodEntry.getKey());
                List<Map<String, Object>> parameters = new ArrayList<>();
                dataMap.put("ps", parameters);
                // 常规请求参数处理
                if (!CollectionUtils.isEmpty(methodEntry.getValue().getParameters())) {
                   for (ParameterDTO parameterDTO : methodEntry.getValue().getParameters()) {
                       parameter = new HashMap<>();
                       parameters.add(parameter);
                       parameter.put("name", parameterDTO.getName());
                       parameter.put("dataType", parameterDTO.getSchema().get("type"));
                       parameter.put("paraType", parameterDTO.getIn());
                       parameter.put("required", parameterDTO.isRequired());
                       parameter.put("description", parameterDTO.getDescription());
                   }
                }
                // requestbody参数处理
                System.out.println("接口方法：" + JSON.toJSONString(methodEntry.getValue()));
                if (!CollectionUtils.isEmpty(methodEntry.getValue().getRequestBody())) {
                    Map<String, JSONObject> requestBody = methodEntry.getValue().getRequestBody();
                    JSONObject content = requestBody.get("content");
                    String requestParaPath = "";
                    if (content != null) {
                        for (String key : content.keySet()) {
                            JSONObject schema = content.getJSONObject(key).getJSONObject("schema");
                            if ("multipart/form-data".equalsIgnoreCase(key)) {
                                if(schema.getJSONObject("properties") != null) {
                                    JSONObject properties = schema.getJSONObject("properties");
                                    for (String propertyName : properties.keySet()) {
                                        parameter = new HashMap<>();
                                        parameters.add(parameter);
                                        parameter.put("paraType", "requestbody");
                                        parameter.put("required", false);
                                        parameter.put("name", propertyName);
                                        parameter.put("dataType", properties.getJSONObject(propertyName).getString("type"));
                                        parameter.put("description", properties.getJSONObject(propertyName).getString("description"));
                                        if (schema.getJSONArray("required") != null) {
                                            if (schema.getJSONArray("required").contains(propertyName)) {
                                                parameter.put("required", true);
                                            }
                                        }
                                    }
                                }
                            } else if ("application/json".equalsIgnoreCase(key)) {
                                parameter = new HashMap<>();
                                parameters.add(parameter);
                                parameter.put("paraType", "requestbody");
                                parameter.put("required", false);
                                if (null == schema.get("type")) {
                                    parameter.put("dataType", "object");
                                    requestParaPath = schema.getString("$ref");
                                } else {
                                    parameter.put("dataType", schema.getString("type"));
                                    if (schema.getJSONObject("items") != null) {
                                        // DTO请求对象类型
                                        requestParaPath = schema.getJSONObject("items").getString("$ref");
                                    } else if(schema.getJSONObject("additionalProperties") != null) {
                                        // Map对象
                                        parameter.put("dataType", "map");
                                        if (schema.getJSONObject("additionalProperties").containsKey("$ref")) {
                                            requestParaPath = schema.getJSONObject("additionalProperties").getString("$ref");
                                        }
                                    }
                                }
                                if (!StringUtils.isEmpty(requestParaPath)) {
                                    String objectName = requestParaPath.split("/")[3];
                                    ObjectDTO objectDTO = swaggerAPIDTO.getComponents().get("schemas").get(objectName);
                                    parameter.put("name", objectName);
                                    JSONObject newRequestBodyPara = new JSONObject();
                                    StringBuffer buf = new StringBuffer();
                                    buf.append("/** \r\n");
                                    for (Map.Entry<String, DataTypeDTO> dataTypeDTOEntry : objectDTO.getProperties().entrySet()) {
                                        if (dataTypeDTOEntry.getValue().getType().equalsIgnoreCase("array")) {
                                            newRequestBodyPara.put(dataTypeDTOEntry.getKey(), new JSONArray());
                                        } else {
                                            newRequestBodyPara.put(dataTypeDTOEntry.getKey(), null);
                                        }
                                        buf.append("* " + dataTypeDTOEntry.getKey() + ":" + dataTypeDTOEntry.getValue().getDescription() + "\r\n");
                                    }
                                    buf.append("**/");
                                    parameter.put("description", newRequestBodyPara.toJSONString() + "\r\n" + buf.toString());
                                }
                            }
                        }
                    }
                }
                JSONObject content = method.getResponses().get("200").getJSONObject("content");
                if (null == content) {
                    dataMap.put("respType", "void");
                    dataMap.put("respDesc", "无返回值");
                    continue;
                }
                JSONObject responseSchema = content.getJSONObject("*/*").getJSONObject("schema");
                List<Map<String, Object>> respParameters = new ArrayList<>();
                dataMap.put("resp", respParameters);
                if (null == responseSchema.get("type")) {
                    if (responseSchema.get("$ref") != null) {
                        String objectName = responseSchema.getString("$ref").split("/")[3];
                        ObjectDTO objectDTO = swaggerAPIDTO.getComponents().get("schemas").get(objectName);
                        for (Map.Entry<String, DataTypeDTO> dataTypeDTOEntry : objectDTO.getProperties().entrySet()) {
                            parameter = new HashMap<>();
                            respParameters.add(parameter);
                            parameter.put("respName", dataTypeDTOEntry.getKey());
                            parameter.put("respType", dataTypeDTOEntry.getValue().getType());
                            parameter.put("respDesc", dataTypeDTOEntry.getValue().getDescription());
                        }
                    }
                } else {
                    if ("array".equalsIgnoreCase(responseSchema.getString("type"))) {
                        JSONObject items = responseSchema.getJSONObject("items");
                        if (items != null && items.get("$ref") != null) {
                            String objectName = items.getString("$ref").split("/")[3];
                            ObjectDTO objectDTO = swaggerAPIDTO.getComponents().get("schemas").get(objectName);
                            for (Map.Entry<String, DataTypeDTO> dataTypeDTOEntry : objectDTO.getProperties().entrySet()) {
                                parameter = new HashMap<>();
                                respParameters.add(parameter);
                                parameter.put("respName", dataTypeDTOEntry.getKey());
                                parameter.put("respType", dataTypeDTOEntry.getValue().getType());
                                parameter.put("respDesc", dataTypeDTOEntry.getValue().getDescription());
                            }
                        }
                    } else {
                        // 普通类型
                        parameter = new HashMap<>();
                        respParameters.add(parameter);
                        parameter.put("respName", "");
                        parameter.put("respType", responseSchema.getString("type"));
                        parameter.put("respDesc", "");
                    }
                }
            }
        }
        String templatePath = this.getClass().getClassLoader().getResource("swagger-word-template.docx").getPath();
        // 注意：此时的doc只有获取到第一个table数据
        // 所以在方法mergeColumns中重新读取本地文件来获取table数据
        // 还有个方案就是重写easypoi的方法
        XWPFDocument doc = WordExportUtil.exportWord07(templatePath, dataMaps);
        String outputPath = Files.createTempFile("swagger2doc", ".docx").toAbsolutePath().toString();
        try(FileOutputStream fos = new FileOutputStream(outputPath)) {
            doc.write(fos);
        }
        // 合并列
        return this.mergeColumns(dataMaps, outputPath);
    }

    /**
     * 合并模板中响应参数多出的列
     *
     * @param dataMaps
     * @param documentPath
     * @throws IOException
     */
    private String mergeColumns(List<Map<String, Object>> dataMaps, String documentPath) throws IOException {
        // 重新读取本地文件，才能获取到所有表格数据
        try(FileInputStream inputStream = new FileInputStream(documentPath)) {
            XWPFDocument document = new XWPFDocument(inputStream);
            // 表格迭代器
            List<XWPFTable> tables = document.getTables();
            int count = tables.size();
            for (int index=0; index< count; index++) {
                XWPFTable table = tables.get(index);
                List<Map<String, Object>> requestParameters = (List<Map<String, Object>>) dataMaps.get(index).get("ps");
                int startRowIndex = TEMPLATE_BASIC_INFO_ROW_COUNT + (CollectionUtils.isEmpty(requestParameters)? 0 : requestParameters.size()) + 2 + 2;
                List<Map<String, Object>> responseParameters = (List<Map<String, Object>>) dataMaps.get(index).get("resp");
                if (CollectionUtils.isEmpty(responseParameters)) {
                    continue;
                }
                int endRowIndex = startRowIndex + responseParameters.size();
                for (int rowIndex = startRowIndex;rowIndex < endRowIndex; rowIndex++) {
                    ExportWordUtils.mergeCellsHorizontal(table, rowIndex, TEMPLATE_MERGE_FORM_COLUMN, TEMPLATE_MERGE_TO_COLUMN);
                }
            }
            // 重新生成文件
            String outputPath = Files.createTempFile("swagger2doc", ".docx").toAbsolutePath().toString();
            try(FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }
            return outputPath;
        }
    }

}
