package org.osate.dfmea.export;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.dfmea.AP;
import org.osate.dfmea.FailureElement;
import org.osate.dfmea.FmeaBuilder;
import org.osate.dfmea.Optimization;
import org.osate.ui.dialogs.Dialog;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class FileExport {

	public static boolean ONLY_FINAL_FAILURE = false;
	public static boolean ONLY_FAILUREMODES = false;
	public static boolean SHOW_INSTANCE_NAME = true;

	EObject root;
	String fileExtension = "xls";

	FmeaBuilder fb;
	File file;
	WritableWorkbook workbook;
	WritableSheet sheet;
	Integer rep_struc_level;

	WritableCellFormat headcf;
	WritableCellFormat normalcf;
	WritableCellFormat repeatedcf;
	WritableCellFormat finalcf;
	Struc_item focus_structure;

	public void ExportFMEAreport(FmeaBuilder fb) {
		this.fb = fb;
		// 创建文件
		file = getReportPathName("fmea", fb.focus_component.ci);
//		file = new File(pathname);
		try {
//			file.createNewFile();
			// 创建工作簿
			workbook = Workbook.createWorkbook(file);
			// 创建sheet
			sheet = workbook.createSheet("SheetName", 0);
			// 设置单元格格式、构造汇报数据结构
			Prepare();
			// 输出第一步（规划与准备）与2-6步的表头
			ReportHead();
			// 输出第二部结构分析
			Report_Structure();
			// 输出3-6步
			Report_Remaining();
			// 最后调整
			Adj_Column_Width();
			// 写文件
			workbook.write();
			workbook.close();
			Dialog.showInfo("Failure Mode and Effect Analysis", "FMEA report generation complete!");

		} catch (IOException e) {
			Dialog.showInfo("IOException e", e.toString());
			e.printStackTrace();
		} catch (WriteException e) {
			Dialog.showInfo("WriteException e", e.toString());
			e.printStackTrace();
		}

	}

	public void Prepare() throws WriteException {
		WritableFont wf = new WritableFont(WritableFont.createFont("Times New Roman"), 11, WritableFont.NO_BOLD, false,
				UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK); // 定义格式 字体 下划线 斜体 粗体 颜色

		headcf = new WritableCellFormat(wf); // 单元格定义
		headcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
		headcf.setBackground(jxl.format.Colour.YELLOW); // 黄色底纹
		headcf.setAlignment(jxl.format.Alignment.CENTRE); // 水平居中
		headcf.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 垂直居中
		headcf.setWrap(true);// 自动换行

		normalcf = new WritableCellFormat(wf); // 单元格定义
		normalcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
		normalcf.setAlignment(jxl.format.Alignment.LEFT); // 水平靠左
		normalcf.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 垂直靠上
		normalcf.setWrap(true);// 自动换行

		repeatedcf = new WritableCellFormat(wf); // 单元格定义
		repeatedcf.setBackground(jxl.format.Colour.GRAY_25); // 灰色底纹
		repeatedcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
		repeatedcf.setAlignment(jxl.format.Alignment.LEFT); // 水平靠左
		repeatedcf.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 垂直靠上
		repeatedcf.setWrap(true);// 自动换行

		finalcf = new WritableCellFormat(wf); // 单元格定义
		finalcf.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW); // 灰色底纹
		finalcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
		finalcf.setAlignment(jxl.format.Alignment.LEFT); // 水平靠左
		finalcf.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 垂直靠上
		finalcf.setWrap(true);// 自动换行

		focus_structure = new Struc_item(fb.focus_component);

	}


	public void ReportHead() throws WriteException, MalformedURLException {

//		sheet.addCell(new Label(0, 0, "规划和准备（步骤1）：", normalcf));
		sheet.addCell(new Label(0, 0, "PLANNING AND PREPARATION (STEP1)", normalcf));
		sheet.addCell(new Label(1, 0, fb.focus_component.getName(), normalcf));
		sheet.mergeCells(1, 0, 7, 0);
		sheet.addCell(new Label(0, 1, null, normalcf));
		sheet.mergeCells(0, 1, 7, 1);

		sheet.addCell(new Label(8, 0, null, normalcf));
		sheet.mergeCells(8, 0, 28, 1);

		int row = 2;
//		sheet.addCell(new Label(0, row, "企业名：", normalcf));
		sheet.addCell(new Label(0, row, "Company Name:", normalcf));
		sheet.mergeCells(0, row, 1, row);
		sheet.addCell(new Label(2, row, fb.head.Company_Name, normalcf));
		sheet.mergeCells(2, row, 7, row);

//		sheet.addCell(new Label(8, row, "项目：", normalcf));
		sheet.addCell(new Label(8, row, "Subject:", normalcf));
		sheet.mergeCells(8, row, 9, row);
		sheet.addCell(new Label(10, row, fb.head.Subject, normalcf));
		sheet.mergeCells(10, row, 15, row);

//		sheet.addCell(new Label(16, row, "DFMEA 识别号：", normalcf));
		sheet.addCell(new Label(16, row, "DFMEA ID Number:", normalcf));
		sheet.mergeCells(16, row, 17, row);
		sheet.addCell(new Label(18, row, fb.head.DFMEA_ID, normalcf));
		sheet.mergeCells(18, row, 28, row);

		row = 3;
//		sheet.addCell(new Label(0, row, "工程地点：", normalcf));
		sheet.addCell(new Label(0, row, "Engineering Location:", normalcf));
		sheet.mergeCells(0, row, 1, row);
		sheet.addCell(new Label(2, row, fb.head.Engineering_Location, normalcf));
		sheet.mergeCells(2, row, 7, row);

//		sheet.addCell(new Label(8, row, "DFMEA-开始日期：", normalcf));
		sheet.addCell(new Label(8, row, "DFMEA Start Date:", normalcf));
		sheet.mergeCells(8, row, 9, row);
		sheet.addCell(new Label(10, row, fb.head.DFMEA_Start_Data, normalcf));
		sheet.mergeCells(10, row, 15, row);

//		sheet.addCell(new Label(16, row, "设计职责：", normalcf));
		sheet.addCell(new Label(16, row, "DFMEA Responsibility:", normalcf));
		sheet.mergeCells(16, row, 17, row);
		sheet.addCell(new Label(18, row, fb.head.Design_Responsibility, normalcf));
		sheet.mergeCells(18, row, 28, row);

		row = 4;
//		sheet.addCell(new Label(0, row, "客户名称：", normalcf));
		sheet.addCell(new Label(0, row, "Customer Name:", normalcf));
		sheet.mergeCells(0, row, 1, row);
		sheet.addCell(new Label(2, row, fb.head.Customer_Name, normalcf));
		sheet.mergeCells(2, row, 7, row);

//		sheet.addCell(new Label(8, row, "DFMEA-修订日期：", normalcf));
		sheet.addCell(new Label(8, row, "DFMEA Revision Date:", normalcf));
		sheet.mergeCells(8, row, 9, row);
		sheet.addCell(new Label(10, row, fb.head.DFMEA_Revision_Data, normalcf));
		sheet.mergeCells(10, row, 15, row);

//		sheet.addCell(new Label(16, row, "保密级别：", normalcf));
		sheet.addCell(new Label(16, row, "Confidentiality Level:", normalcf));
		sheet.mergeCells(16, row, 17, row + 1);
		sheet.addCell(new Label(18, row, fb.head.Confidentiality_Level, normalcf));
		sheet.mergeCells(18, row, 28, row + 1);

		row = 5;
//		sheet.addCell(new Label(0, row, "型号年份/平台：", normalcf));
		sheet.addCell(new Label(0, row, "Model Year(s)/Program(s):", normalcf));
		sheet.mergeCells(0, row, 1, row);
		sheet.addCell(new Label(2, row, fb.head.Model_Year_Program, normalcf));
		sheet.mergeCells(2, row, 7, row);

//		sheet.addCell(new Label(8, row, "跨职能团队：", normalcf));
		sheet.addCell(new Label(8, row, "Cross-Function Team:", normalcf));
		sheet.mergeCells(8, row, 9, row);
		sheet.addCell(new Label(10, row, fb.head.Cross_Func_Team, normalcf));
		sheet.mergeCells(10, row, 15, row);

		row = 6;
//		sheet.addCell(new Label(0, row, "结构分析（步骤2）", headcf));
		sheet.addCell(new Label(0, row, "STRUCTURE ANALYSIS (STEP2)", headcf));
		sheet.mergeCells(0, row, 2, row);
//		sheet.addCell(new Label(3, row, "功能分析（步骤3）", headcf));
		sheet.addCell(new Label(3, row, "FUNCTION ANALYSIS (STEP3)", headcf));
		sheet.mergeCells(3, row, 5, row);
//		sheet.addCell(new Label(6, row, "故障分析（步骤4）", headcf));
		sheet.addCell(new Label(6, row, "FAILURE ANALYSIS (STEP4)", headcf));
		sheet.mergeCells(6, row, 10, row);
//		sheet.addCell(new Label(11, row, "风险分析（步骤5）", headcf));
		sheet.addCell(new Label(11, row, "DFMEA RISK ANALYSIS (STEP5)", headcf));
		sheet.mergeCells(11, row, 16, row);
//		sheet.addCell(new Label(17, row, "优化（步骤6）", headcf));
		sheet.addCell(new Label(17, row, "DFMEA OPTIMIZATION (STEP6)", headcf));
		sheet.mergeCells(17, row, 28, row);

		row = 7;
//		sheet.addCell(new Label(0, row, "1.下一个更高的级别", normalcf));
//		sheet.addCell(new Label(1, row, "2.焦点元素", normalcf));
//		sheet.addCell(new Label(2, row, "3.下一个较低的级别或特征", normalcf));
//		sheet.addCell(new Label(3, row, "1.下一个更高级别的功能和需求", normalcf));
//		sheet.addCell(new Label(4, row, "2.焦点组件的功能和需求", normalcf));
//		sheet.addCell(new Label(5, row, "3.下一个较低的级别的功能和需求或特征", normalcf));
//		sheet.addCell(new Label(6, row, "1.下一个更高级别的元素和/或最终用户的故障后果（FE）", normalcf));
//		sheet.addCell(new Label(7, row, "S", normalcf));
//		sheet.addCell(new Label(8, row, "SC", normalcf));
//		sheet.addCell(new Label(9, row, "2.焦点元素的故障类型（FM）", normalcf));
//		sheet.addCell(new Label(10, row, "3.下一个较低级别的元素或特性的故障原因（FC）", normalcf));
//		sheet.addCell(new Label(11, row, "故障原因（FC）的现有防范措施（PC）", normalcf));
//		sheet.addCell(new Label(12, row, "O", normalcf));
//		sheet.addCell(new Label(13, row, "故障原因（FC）或故障类型（FM）的现有防范措施（DC）", normalcf));
//		sheet.addCell(new Label(14, row, "D", normalcf));
//		sheet.addCell(new Label(15, row, "AP", normalcf));
//		sheet.addCell(new Label(16, row, "FC", normalcf));
//		sheet.addCell(new Label(17, row, "DFMEA防范措施", normalcf));
//		sheet.addCell(new Label(18, row, "DFMEA发现措施", normalcf));
//		sheet.addCell(new Label(19, row, "负责人姓名", normalcf));
//		sheet.addCell(new Label(20, row, "计划完成日期", normalcf));
//		sheet.addCell(new Label(21, row, "状态", normalcf));
//		sheet.addCell(new Label(22, row, "有证据的决定性措施", normalcf));
//		sheet.addCell(new Label(23, row, "完成日期", normalcf));
//		sheet.addCell(new Label(24, row, "S", normalcf));
//		sheet.addCell(new Label(25, row, "O", normalcf));
//		sheet.addCell(new Label(26, row, "D", normalcf));
//		sheet.addCell(new Label(27, row, "AP", normalcf));
//		sheet.addCell(new Label(28, row, "备注", normalcf));

		sheet.addCell(new Label(0, row, "1. Next Higher Level", normalcf));
		sheet.addCell(new Label(1, row, "2. Focus Element", normalcf));
		sheet.addCell(new Label(2, row, "3. Next Lower Level or Characteristic Type", normalcf));
		sheet.addCell(new Label(3, row, "1. Next Higher Level Function and Requirement", normalcf));
		sheet.addCell(new Label(4, row, "2. Focus Element Function and Requirement", normalcf));
		sheet.addCell(new Label(5, row, "3. Next Lower Level Function and Requirement or Characteristic", normalcf));
		sheet.addCell(new Label(6, row, "1. Failure Effects (FE) to the Next Higher Level Element and/or End User",
				normalcf));
		sheet.addCell(new Label(7, row, "S", normalcf));
		sheet.addCell(new Label(8, row, "SC", normalcf));
		sheet.addCell(new Label(9, row, "2. Failure Mode (FM) of the Focus Element", normalcf));
		sheet.addCell(
				new Label(10, row, "3. Failure Cause (FC) of the Next Lower Element or Characteristic", normalcf));
		sheet.addCell(new Label(11, row, "Current Prevention Control (PC) of FC", normalcf));
		sheet.addCell(new Label(12, row, "O", normalcf));
		sheet.addCell(new Label(13, row, "Current Detection (DC) of FC or FM", normalcf));
		sheet.addCell(new Label(14, row, "D", normalcf));
		sheet.addCell(new Label(15, row, "AP", normalcf));
		sheet.addCell(new Label(16, row, "FC", normalcf));
		sheet.addCell(new Label(17, row, "DFMEA Preventive Action", normalcf));
		sheet.addCell(new Label(18, row, "DFMEA Detection Action", normalcf));
		sheet.addCell(new Label(19, row, "Responsible Person's Name", normalcf));
		sheet.addCell(new Label(20, row, "Target Completion Date", normalcf));
		sheet.addCell(new Label(21, row, "Status", normalcf));
		sheet.addCell(new Label(22, row, "Action Taken with Pointer to Evidence", normalcf));
		sheet.addCell(new Label(23, row, "Completion Date", normalcf));
		sheet.addCell(new Label(24, row, "S", normalcf));
		sheet.addCell(new Label(25, row, "O", normalcf));
		sheet.addCell(new Label(26, row, "D", normalcf));
		sheet.addCell(new Label(27, row, "AP", normalcf));
		sheet.addCell(new Label(28, row, "Remarks", normalcf));
	}

	public void Report_Structure() throws RowsExceededException, WriteException {
		int absrow = 8 - 1;
		String prefix = null;
		// 1.下一个更高的级别
		int row = 1;
		int column = 0;
		System.out.println("Struct::" + focus_structure.mystruc.getName() + "  Maxrows::" + focus_structure.maxrows);
		System.out.println("Supstruc size::" + focus_structure.supstruc.size());
		if (focus_structure.supstruc.size() > 0) {
			for (int i = 0; i < focus_structure.supstruc.size(); i++) {
				prefix = getprefix(focus_structure.levelmap.get(focus_structure.supstruc.get(i)) - 1, "<");
				sheet.addCell(
						new Label(column, absrow + row, prefix + focus_structure.supstruc.get(i).getName(), normalcf));
				if (i == focus_structure.supstruc.size() - 1 && row < focus_structure.maxrows) {
					sheet.mergeCells(column, absrow + row, column, absrow + focus_structure.maxrows);
				}
				row++;
			}
		} else {
			sheet.addCell(new Label(column, absrow + row, "", normalcf));
			sheet.mergeCells(column, absrow + row, column, absrow + focus_structure.maxrows);
		}

		// 2.焦点元素
		row = 1;
		column = 1;
		sheet.addCell(new Label(column, absrow + row, focus_structure.mystruc.getName(), normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + focus_structure.maxrows);

		// 3.下一个较低的级别或特征
		row = 1;
		column = 2;
		System.out.println("Substruc size::" + focus_structure.substruc.size());
		if (focus_structure.substruc.size() > 0) {
			for (int i = 0; i < focus_structure.substruc.size(); i++) {
				prefix = getprefix(focus_structure.levelmap.get(focus_structure.substruc.get(i)) - 1, ">");
				sheet.addCell(
						new Label(column, absrow + row, prefix + focus_structure.substruc.get(i).getName(),
						normalcf));
				if (i == focus_structure.substruc.size() - 1 && row < focus_structure.maxrows) {
					sheet.mergeCells(column, absrow + row, column, absrow + focus_structure.maxrows);
				}
				row++;
			}
		} else {
			sheet.addCell(new Label(column, absrow + row, "", normalcf));
			sheet.mergeCells(column, absrow + row, column, absrow + focus_structure.maxrows);
		}
	}

	public void Report_Remaining() throws RowsExceededException, WriteException {

		int absrow = 8 - 1;
		System.out.println("Function nums::" + focus_structure.funcitems.size());
		if (focus_structure.funcitems.size() > 0) {

			for (Func_item fci : focus_structure.funcitems) {
				Report_Fuc(fci, absrow);
				absrow = absrow + fci.maxrows;
			}

		} else {
			for (int i = 3; i <= 28; i++) {
				sheet.addCell(new Label(i, absrow + 1, "", normalcf));
				sheet.mergeCells(i, absrow + 1, i, absrow + focus_structure.maxrows);
			}
		}


	}

	public void Report_Fuc(Func_item fci, int absrow) throws RowsExceededException, WriteException {
		String prefix = null;
		// 1.下一个更高级别的功能和需求
		System.out.println("		Function::" + fci.myfunc.funcname + "  Maxrows::" + fci.maxrows);
		System.out.println("		Supfunc size::" + fci.supfunc.size());
		int row = 1;
		int column = 3;
		if (fci.supfunc.size() > 0) {
			for (int i = 0; i < fci.supfunc.size(); i++) {
				prefix = getprefix(fci.suplevelmap.get(i) - 1, "<");
				if (SHOW_INSTANCE_NAME) {
					prefix += "\"" + fci.supfunc.get(i).ref_component.getName() + "\": ";
				}
				sheet.addCell(new Label(column, absrow + row, prefix + fci.supfunc.get(i).funcname, normalcf));
				if (i == fci.supfunc.size() - 1 && row < fci.maxrows) {
					sheet.mergeCells(column, absrow + row, column, absrow + fci.maxrows);
				}
				row++;
			}
		} else {
			sheet.addCell(new Label(column, absrow + row, "", normalcf));
			sheet.mergeCells(column, absrow + row, column, absrow + fci.maxrows);
		}

		// 2.焦点组件的功能和需求
		row = 1;
		column = 4;
		sheet.addCell(new Label(column, absrow + row, fci.myfunc.funcname, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + fci.maxrows);

		// 3.下一个较低的级别的功能和需求或特征
		row = 1;
		column = 5;
		System.out.println("		Subfunc size::" + fci.subfunc.size());
		if (fci.subfunc.size() > 0) {
			for (int i = 0; i < fci.subfunc.size(); i++) {
				prefix = getprefix(fci.sublevelmap.get(i) - 1, ">");
				if (SHOW_INSTANCE_NAME) {
					prefix += "\"" + fci.subfunc.get(i).ref_component.getName() + "\": ";
				}
				sheet.addCell(new Label(column, absrow + row, prefix + fci.subfunc.get(i).funcname, normalcf));
				if (i == fci.subfunc.size() - 1 && row < fci.maxrows) {
					sheet.mergeCells(column, absrow + row, column, absrow + fci.maxrows);
				}
				row++;
			}
		} else {
			sheet.addCell(new Label(column, absrow + row, "", normalcf));
			sheet.mergeCells(column, absrow + row, column, absrow + fci.maxrows);
		}


		System.out.println("		error nums::" + fci.erroritems.size());
		if (fci.erroritems.size() > 0) {
			for (Error_item eri : fci.erroritems) {
				Report_Error(eri, absrow);
				absrow = absrow + eri.maxrows;
			}
		} else {
			for (int i = 6; i <= 28; i++) {
				sheet.addCell(new Label(i, absrow + 1, "", normalcf));
				sheet.mergeCells(i, absrow + 1, i, absrow + fci.maxrows);
			}
		}
	}

	public void Report_Error(Error_item eri, int absrow) throws RowsExceededException, WriteException {
		String prefix = null;

		int row = 1;
		int column = 6;
		int maxS = 0;
		System.out.println("				Error::" + eri.myerror.name + "  Maxrows::" + eri.maxrows + "  Ref_S::"
				+ eri.myerror.ref_S);
		System.out.println("				Superror size::" + eri.superror.size());
		if (eri.superror.size() > 0) {
			for (int i = 0; i < eri.superror.size(); i++) {
				prefix = getprefix(eri.suplevelmap.get(i) - 1, "<");
				if (SHOW_INSTANCE_NAME) {
					prefix += "\"" + eri.superror.get(i).ref_component.getName() + "\": ";
				}
				WritableCellFormat formate = normalcf;
				// 1.下一个更高级别的元素或最终用户的故障后果
				if (eri.suprepeated.get(i) == true) {
					formate = repeatedcf;
				}
				if (eri.superror.get(i).isFailureMode() && eri.superror.get(i).isFinalEffect()) {
					formate = finalcf;
				}
				sheet.addCell(new Label(column, absrow + row, prefix + eri.superror.get(i).name, formate));
				// S
				prefix = ((eri.superror.get(i).fmea_serverity == null) ? "" : "" + eri.superror.get(i).fmea_serverity);
				if (eri.superror.get(i).fmea_serverity != null) {
					maxS=Math.max(maxS,eri.superror.get(i).fmea_serverity);
				}
				sheet.addCell(new Label(column + 1, absrow + row, prefix, normalcf));
				// SC
				sheet.addCell(new Label(column + 2, absrow + row, "", normalcf));
				if (i == eri.superror.size() - 1 && row < eri.maxrows) {
					sheet.mergeCells(column, absrow + row, column, absrow + eri.maxrows);
					sheet.mergeCells(column + 1, absrow + row, column + 1, absrow + eri.maxrows);
					sheet.mergeCells(column + 2, absrow + row, column + 2, absrow + eri.maxrows);
				}
				row++;
			}
		} else {
			sheet.addCell(new Label(column, absrow + row, "", normalcf));
			sheet.mergeCells(column, absrow + row, column, absrow + eri.maxrows);
			sheet.addCell(new Label(column + 1, absrow + row, "", normalcf));
			sheet.mergeCells(column + 1, absrow + row, column + 1, absrow + eri.maxrows);
			sheet.addCell(new Label(column + 2, absrow + row, "", normalcf));
			sheet.mergeCells(column + 2, absrow + row, column + 2, absrow + eri.maxrows);
		}

		// 2.焦点元素的故障类型
		row = 1;
		column = 9;
		sheet.addCell(new Label(column, absrow + row, eri.myerror.name, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + eri.maxrows);

		// 3.下一个较低的级别元素或特性的故障原因
		row = 1;
		column = 10;
		System.out.println("				Suberror size::" + eri.suberror.size());
		if (eri.suberror.size() > 0) {
			for (int i = 0; i < eri.suberror.size(); i++) {
				int rows = Math.max(eri.suberror.get(i).optimizations.size(), 1);
				prefix = getprefix(eri.sublevelmap.get(i) - 1, ">");
				if (SHOW_INSTANCE_NAME) {
					prefix += "\"" + eri.suberror.get(i).ref_component.getName() + "\": ";
				}
				WritableCellFormat formate = normalcf;
				if (eri.subrepeated.get(i) == true) {
					formate = repeatedcf;
				}
				if (eri.suberror.get(i).isFailureMode() && eri.suberror.get(i).isFinalCause()) {
					formate = finalcf;
				}
				sheet.addCell(new Label(column, absrow + row, prefix + eri.suberror.get(i).name, formate));
				if (rows >= 2) {
					sheet.mergeCells(column, absrow + row, column, absrow + row + rows - 1);
				} else if (i == eri.suberror.size() - 1 && row < eri.maxrows) {
					sheet.mergeCells(column, absrow + row, column, absrow + eri.maxrows);
				}
				row = row + rows;
			}
		} else {
			sheet.addCell(new Label(column, absrow + row, "", normalcf));
			sheet.mergeCells(column, absrow + row, column, absrow + eri.maxrows);
		}


		if (eri.suberror.size() > 0) {
			int rowcounter = 0;
			for (int i = 0; i < eri.suberror.size(); i++) {
				FailureElement fmi = eri.suberror.get(i);
				int rownums = eri.rowandrownums.get(i).getValue();
				if (i == eri.suberror.size() - 1) {
					rownums = eri.maxrows - rowcounter;
				}
				rowcounter += rownums;
				Report_Risk(fmi, absrow, rownums, maxS == 0 ? null : maxS);
				absrow = absrow + rownums;
			}
		} else {
			for (int i = 11; i <= 28; i++) {
				sheet.addCell(new Label(i, absrow + 1, "", normalcf));
				sheet.mergeCells(i, absrow + 1, i, absrow + eri.maxrows);
			}
		}

	}

	public void Report_Risk(FailureElement fmi, int absrow, int rownums, Integer ref_S)
			throws RowsExceededException, WriteException {
		int row = 1;
		int column = 11;
		String text = fmi.prevention_control == null ? "" : fmi.prevention_control;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 12;
		text = fmi.fmea_occurrence == null ? "" : "" + fmi.fmea_occurrence;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 13;
		text = fmi.detection_control == null ? "" : fmi.detection_control;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 14;
		text = fmi.fmea_detection == null ? "" : "" + fmi.fmea_detection;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 15;
		AP ap = null;
		if (fmi.fmea_occurrence != null && fmi.fmea_detection != null && ref_S != null) {
			ap = FmeaBuilder.CalculateAp(ref_S, fmi.fmea_occurrence, fmi.fmea_detection);
		}
		text = ap == null ? "" : "" + ap.getPrintname();
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 16;
		text = "";
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		if (fmi.optimizations.size() > 0) {
			for (int i = 0; i < fmi.optimizations.size(); i++) {
				int oprownums = 1;
				if (i == fmi.optimizations.size() - 1) {
					oprownums = rownums;
				}
				Report_Optimization(fmi.optimizations.get(i), absrow, oprownums, ref_S);
				absrow++;
				rownums--;
			}
		} else {
			for (int i = 17; i <= 28; i++) {
				sheet.addCell(new Label(i, absrow + 1, "", normalcf));
				sheet.mergeCells(i, absrow + 1, i, absrow + +row + rownums - 1);
			}
		}

	}

	public void Report_Optimization(Optimization opi, int absrow, int rownums, Integer ref_S)
			throws RowsExceededException, WriteException {
		int row = 1;
		int column = 17;
		String text = opi.opt_pc == null ? "" : opi.opt_pc;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 18;
		text = opi.opt_dc == null ? "" : opi.opt_dc;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 19;
		text = opi.respons_person == null ? "" : opi.respons_person;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 20;
		text = opi.target_completion_data == null ? "" : opi.target_completion_data;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 21;
		text = opi.status == null ? "" : opi.status;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 22;
		text = opi.evidence == null ? "" : opi.evidence;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 23;
		text = opi.completion_data == null ? "" : opi.completion_data;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 24;
		text = ref_S == null ? "" : "" + ref_S;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 25;
		text = opi.opt_occurrence == null ? "" : "" + opi.opt_occurrence;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);



		column = 26;
		text = opi.opt_detection == null ? "" : "" + opi.opt_detection;
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 27;
		AP ap = null;
		if (opi.opt_occurrence != null && opi.opt_detection != null && ref_S != null) {
			ap = FmeaBuilder.CalculateAp(ref_S, opi.opt_occurrence, opi.opt_detection);
		}
		text = ap == null ? "" : "" + ap.getPrintname();
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

		column = 28;
		text = "";
		sheet.addCell(new Label(column, absrow + row, text, normalcf));
		sheet.mergeCells(column, absrow + row, column, absrow + +row + rownums - 1);

	}

	public String getprefix(int num, String s) {
		String result = "";
		for (int i = 1; i <= num; i++) {
			result=result+s;
		}
		return result;
	}

	public void Adj_Column_Width() {
		CellView cellView = new CellView();
		List<Integer> list = Stream.iterate(0, item -> item + 1).limit(28).collect(Collectors.toList());
		int[] data = { 7, 8, 12, 14, 15, 16, 21, 23, 24, 25, 26, 27 };
		List<Integer> notadj = Arrays.stream(data).boxed().collect(Collectors.toList());
		list.removeAll(notadj);
		cellView.setAutosize(true); // 设置自动大小
		for (int i : list) {
			sheet.setColumnView(i, cellView);
		}
	}



	public File getReportPathName(String reporttype, ComponentInstance root) {

		String filename = null;
		reporttype = reporttype.replaceAll(" ", "");
		Resource res = root.eResource();
		URI uri = res.getURI();
		IPath path = OsateResourceUtil.toIFile(uri).getFullPath();
		path = path.removeFileExtension();

		filename = root instanceof SystemInstance
				? root.getComponentClassifier().getName().replaceAll("::", "-").replaceAll("\\.", "-")
				: root.getComponentInstancePath().replaceAll("::", "-").replaceAll("\\.", "-");
		filename += "_of_" + path.lastSegment();
		filename += "_" + reporttype;

		path = path.removeLastSegments(1).append("/reports/" + reporttype + "/" + filename);
		path = path.addFileExtension(fileExtension);
		AadlUtil.makeSureFoldersExist(path);

		// create report file
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		File file = ifile.getLocation().toFile();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Dialog.showInfo("IOException e", e.toString());
			e.printStackTrace();
		}
		return file;
	}


}
