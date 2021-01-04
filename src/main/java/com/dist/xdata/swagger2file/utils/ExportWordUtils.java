package com.dist.xdata.swagger2file.utils;

import cn.afterturn.easypoi.word.WordExportUtil;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
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

    /**
     * 得到Cell的CTTcPr,不存在则新建。
     *
     * @param cell XWPFTableCell
     * @return
     */
    private static CTTcPr getCellCTTcPr(XWPFTableCell cell) {
        CTTc cttc = cell.getCTTc();
        CTTcPr tcPr = cttc.isSetTcPr() ? cttc.getTcPr() : cttc.addNewTcPr();
        return tcPr;
    }

    /**
     * 合并列
     *
     * @param table 每个word表
     * @param row 要合并的列所在行号
     * @param fromCell 起始单元格号
     * @param toCell 结束单元格号
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if (null == cell) {
                continue;
            }
            if (cellIndex == fromCell) {
                getCellCTTcPr(cell).addNewHMerge().setVal(STMerge.RESTART);
            } else {
                getCellCTTcPr(cell).addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }
}