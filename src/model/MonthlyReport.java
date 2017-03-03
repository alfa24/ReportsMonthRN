package model;

import controller.Controller;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * Created by Александр on 02.01.2017.
 * Формирует Ежемесячный отчет
 */
public class MonthlyReport {
    private String fileName;
    private Config config;
    private ReportWorks reportWorks;
    private HSSFWorkbook workBookTemplate;
    private Controller controller;

    public MonthlyReport(ReportWorks reportWorks, Config config, Controller controller) throws IOException {
        this.config = config;
        this.fileName = Constants.templateMonthReportsPath;
        this.reportWorks = reportWorks;
        this.controller = controller;
        FileInputStream inputStream = null;

        inputStream = new FileInputStream(fileName);
        this.workBookTemplate = new HSSFWorkbook(inputStream);
    }

    //Создаем новый файл Excel
    public void create(String fileName) throws IOException {
        ArrayList<String> contractors;
        contractors = reportWorks.getContractors();
        for (String name :
                contractors) {
            CreateReports(name);
        }
    }

    private void CreateReports(String name) throws IOException {
        HSSFWorkbook workBookOutput = new HSSFWorkbook();

        fillReport(name, workBookOutput);

        //каталог для файлов
        String fio = name;
        fio = fio.substring(0, fio.indexOf(" "));
        File file = new File(Paths.get(config.getPath_out_files(), reportWorks.getPeriod("MM.yyyy"), fio).toString());
        file.mkdirs();
        File flename = Paths.get(file.toString(), reportWorks.getPeriod("yyyy-MM") + "_Ежемесячный отчет ОТП АЗК и АЗС УОП НПО ИРПУ. " + fio + ".xls").toFile();
        OutputStream outputStream = new FileOutputStream(flename);
        workBookOutput.write(outputStream);
        controller.addToLog("Создан файл: " + flename.toString());
    }

    //Заполняем файл данными из базы
    private void fillReport(String name, HSSFWorkbook workBookOutput) {
        HSSFSheet sheetTemplate = workBookTemplate.getSheetAt(0);
        HSSFSheet sheetOutput = workBookOutput.createSheet();

        int rowNum = 0;
        for (int i = 0; i < 20; i++) {
            sheetOutput.setColumnWidth(i, sheetTemplate.getColumnWidth(i));
        }

        HSSFRow row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(0), row);
        row.getCell(0).setCellValue(row.getCell(0).getStringCellValue() + " " + reportWorks.getPeriod("MM.yyyy"));

        //Общее количество заявок
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(1), row);
        row.getCell(1).setCellValue(reportWorks.getCountWorks());

        //Кол-во заявок по системе управления
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(2), row);
        String val = String.valueOf(reportWorks.getCountWorks() - reportWorks.getWorks(name, WorksCategory.VIDEO_MONITORING).size());
        row.getCell(1).setCellValue(val);

        //Кол-во заявок по системе видеонаблюдения
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(3), row);
        val = String.valueOf(reportWorks.getWorks(name, WorksCategory.VIDEO_MONITORING).size());
        row.getCell(1).setCellValue(val);

        //Кол-во заявок по нефтебазе
        ArrayList<Works> works = reportWorks.getWorks(name, WorksCategory.NB);
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(4), row);
        val = String.valueOf(works.size());
        row.getCell(1).setCellValue(val);

        //Кол-во заявок с выездом
        works = reportWorks.getWorks(name, WorksCategory.DRIVE);
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(5), row);
        val = String.valueOf(works.size());
        row.getCell(1).setCellValue(val);

        //Номера заявок с выездом
        rowNum = fillWorkToCells(6, rowNum, sheetTemplate, sheetOutput, name, WorksCategory.DRIVE);

        //Номера заявок с нерабочее время и праздничные дни
        rowNum = fillWorkToCells(7, rowNum, sheetTemplate, sheetOutput, name, WorksCategory.WEEKEND);

        //Номера заявок с простоями АЗК
        rowNum = fillWorkToCells(8, rowNum, sheetTemplate, sheetOutput, name, WorksCategory.STOP);

        //Номера заявок на установку оборудования
        rowNum = fillWorkToCells(9, rowNum, sheetTemplate, sheetOutput, name, WorksCategory.HARDWARE_INSTALL);

        //Номера заявок с ТО
        rowNum = fillWorkToCells(10, rowNum, sheetTemplate, sheetOutput, name, WorksCategory.TO);

        //Актуализация паспортов
        works = reportWorks.getWorks(name, WorksCategory.TO);
        for (Works work :
                works) {
            rowNum++;
            row = sheetOutput.createRow(rowNum);
            copyRow(sheetTemplate.getRow(11), row);
            if (works.indexOf(work) > 0) {
                row.getCell(0).setCellValue("");
            }
            row.getCell(2).setCellValue(work.getDate());
            row.getCell(3).setCellValue(work.getAzk());
        }

        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(14), row);
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(14), row);
        rowNum++;
        row = sheetOutput.createRow(rowNum);
        copyRow(sheetTemplate.getRow(15), row);
    }

    //копирует номер строки rowFrom из листа sheetTemplate
    //вставляет эту строку начиная со строки rowTo в листе sheetOutput
    //заполняет в цикле работами из списка reportWorks с фильром по категории работы category
    private int fillWorkToCells(int rowFrom, int rowTo, HSSFSheet sheetTemplate, HSSFSheet sheetOutput, String name, WorksCategory category) {
        ArrayList<Works> works = reportWorks.getWorks(name, category);
        for (Works work :
                works) {
            rowTo++;
            HSSFRow row = sheetOutput.createRow(rowTo);
            copyRow(sheetTemplate.getRow(rowFrom), row);
            if (works.indexOf(work) > 0) {
                row.getCell(0).setCellValue("");
            }
            row.getCell(1).setCellValue(work.getNumber());
            row.getCell(2).setCellValue(work.getDate());
            row.getCell(3).setCellValue(work.getAzk());
        }
        return rowTo;
    }

    //метод создает копию строки sourceRow
    private void copyRow(HSSFRow sourceRow, HSSFRow newRow) {
        //цикл по заполненным колонкам строки
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            HSSFCell oldCell = sourceRow.getCell(i);
            HSSFCell newCell;

            //если копируемая существует
            if (oldCell == null) {
                continue;
            } else {
                newCell = newRow.createCell(i);
            }

            //копируем стили оформления ячейки
            HSSFCellStyle newCellStyle = newRow.getSheet().getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(newCellStyle);

            //если есть комментарий
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            //если есть гиперссылк
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            if (i == 0) {
                newCell.setCellValue(oldCell.getStringCellValue());
            }
        }

        // копируем регионы объединения ячеек
        for (int i = 0; i < sourceRow.getSheet().getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = sourceRow.getSheet().getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
                        (newRow.getRowNum() +
                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
                                )),
                        cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn());
                newRow.getSheet().addMergedRegion(newCellRangeAddress);
            }
        }
        newRow.setHeight(sourceRow.getHeight());
    }
}
