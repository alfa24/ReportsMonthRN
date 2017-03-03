package model;

import controller.Controller;
import exceptions.DriverNotFoundExceptions;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by ФаличевАЮ on 23.01.2017.
 * Формирует отчет по расходу топлива на выездных работах
 * Создает маршрутные листы и общий отчет по ГСМ (перечень маршрутных листов)
 */
public class ReportFuelFlow {

    private Controller controller;
    private Config config;
    private ReportWorks reportWorks;
    private HSSFWorkbook workBookReportGSM;
    private HSSFWorkbook workBookML;

    public ReportFuelFlow(ReportWorks reportWorks, Config config, Controller controller) throws IOException {
        this.config = config;
        this.reportWorks = reportWorks;
        this.controller = controller;
        FileInputStream inputStream = null;

        inputStream = new FileInputStream(Constants.tmplReportGSM);
        this.workBookReportGSM = new HSSFWorkbook(inputStream);

        inputStream = new FileInputStream(Constants.tmplML);
        this.workBookML = new HSSFWorkbook(inputStream);
    }

    public void create() throws IOException {

        ArrayList<String> contractors;
        contractors = reportWorks.getContractors();
        for (String name :
                contractors) {
            ArrayList<Works> works = reportWorks.getWorks(name, WorksCategory.FUELFLOW);
            if (works.size() > 0) {
                try {
                    CreateReports(name, works);
                } catch (DriverNotFoundExceptions driverNotFoundExceptions) {
                    controller.addToLog(driverNotFoundExceptions.getMessage());
                }
            }
        }


    }

    private void CreateReports(String contractor, ArrayList<Works> works) throws IOException, DriverNotFoundExceptions {
        Config.Driver driver = config.getDriver(contractor);
        if (driver == null) {
            throw new DriverNotFoundExceptions("Не найдены данные по " + contractor + " в списке Водителей. Отчет по Маршрутным листам для него не создан.");
        }


        HSSFSheet wbReportGsmSheet = workBookReportGSM.getSheetAt(0);
        HSSFWorkbook wbOutputReportGSM = new HSSFWorkbook();
        HSSFSheet wbOutReportGsmSheet = wbOutputReportGSM.createSheet();
        HSSFRow row;

        int rowFrom = 15;
        int rowTo = rowFrom;
        for (int i = 0; i < 20; i++) {
            wbOutReportGsmSheet.setColumnWidth(i, wbReportGsmSheet.getColumnWidth(i));
        }

        for (int i = 0; i < rowFrom; i++) {
            row = wbOutReportGsmSheet.createRow(i);
            copyRow(wbReportGsmSheet.getRow(i), row);
        }

        //период
        row = wbOutReportGsmSheet.getRow(8);
        row.getCell(0).setCellValue("За " + reportWorks.getPeriod("MM.yyyy") + " года");
        //размер компенсации
        row.getCell(3).setCellValue("Размер компенсации: " + driver.getPayout(reportWorks.getFirstDate("MM")) + " руб./1 км");

        //водитель
        row = wbOutReportGsmSheet.getRow(9);
        row.getCell(2).setCellValue(driver.getName());

        //авто
        row = wbOutReportGsmSheet.getRow(10);
        row.getCell(2).setCellValue(driver.getAuto());
        row = wbOutReportGsmSheet.getRow(11);
        row.getCell(2).setCellValue(driver.getNumberAuto());


        Double sumDistance = 0.0;
        Double payout = 0.0;

        //каталог для файлов
        String fio = driver.getName();
        fio = fio.substring(0, fio.indexOf(" "));
        File file = new File(Paths.get(config.getPath_out_files(), reportWorks.getPeriod("MM.yyyy"), fio, "ГСМ").toString());
        file.mkdirs();

        for (Works work :
                works) {
            //создаем маршрутный лист на эту работу
            createML(work, file);
            row = wbOutReportGsmSheet.createRow(rowTo);
            copyRow(wbReportGsmSheet.getRow(rowFrom), row);

            //дата
            String val = work.getDate("dd.MM.YY");
            row.getCell(0).setCellValue(val);

            //№ Листа и АЗК
            val = work.getNumber() + " (" + work.getAzk() + ")";
            row.getCell(1).setCellValue(val);

            //№ Пробег и компенсация
            Double distance = config.getDistance(work.getAzk()) * 2;
            sumDistance += distance;
            row.getCell(2).setCellValue(distance);
            payout = config.getPayout(work.getContractor(), work.getDate("MM"));
            row.getCell(3).setCellValue(payout * distance);

            rowTo++;
        }

        for (int i = rowTo; i < rowTo + 10; i++) {
            rowFrom++;
            row = wbOutReportGsmSheet.createRow(i);
            copyRow(wbReportGsmSheet.getRow(rowFrom), row);
        }

        //итоги
        row = wbOutReportGsmSheet.getRow(rowTo);
        row.getCell(2).setCellValue(sumDistance);
        row.getCell(3).setCellValue(payout * sumDistance);

        //Подпись
        row = wbOutReportGsmSheet.getRow(rowTo + 5);
        row.getCell(0).setCellValue(driver.getName() + " /___________________/_'____'__________________201__  года.");

        File fileName = Paths.get(file.toString(), fio + ". Отчет по МЛ " + reportWorks.getPeriod("MM.yyyy") + ".xls").toFile();
        OutputStream outputStream = new FileOutputStream(fileName);
        wbOutputReportGSM.write(outputStream);
        controller.addToLog("Создан файл: " + fileName.toString());
    }

    private void createML(Works work, File file) throws IOException {
        HSSFRow row;
        String val;
        HSSFSheet sheetTemplate = workBookML.getSheetAt(0);
        StringBuilder mlFileName = new StringBuilder("МЛ_");

        //Водитель
        row = sheetTemplate.getRow(13);
        val = String.valueOf(work.getContractor());
        row.getCell(2).setCellValue(val);
        String fio = val.substring(0, val.indexOf(" "));
        mlFileName.append(fio).append("_");

        Config.Driver driver = config.getDriver(val);
        if (driver != null) {
            //Марка Авто
            row = sheetTemplate.getRow(11);
            row.getCell(5).setCellValue(driver.getAuto());

            //Номер Авто
            row = sheetTemplate.getRow(12);
            row.getCell(8).setCellValue(driver.getNumberAuto());
        }

        //Дата
        row = sheetTemplate.getRow(6);
        val = work.getDate("dd");
        row.getCell(4).setCellValue(val);
        val = work.getDate("MM");
        row.getCell(6).setCellValue(val);
        val = work.getDate("YYYY");
        row.getCell(10).setCellValue(val);
        val = work.getDate("dd.MM.YY");
        mlFileName.append(val).append("_");

        //Номер заявки
        row = sheetTemplate.getRow(4);
        val = String.valueOf(work.getNumber());
        row.getCell(22).setCellValue(val);
        row = sheetTemplate.getRow(24);
        row.getCell(10).setCellValue("№" + val);
        mlFileName.append(val).append("(");

        //Номер АЗК
        row = sheetTemplate.getRow(24);
        val = String.valueOf(work.getAzk());
        row.getCell(0).setCellValue(val);
        mlFileName.append(val).append(")");

        File fileName = Paths.get(file.toString(), mlFileName + ".xls").toFile();
        OutputStream outputStream = new FileOutputStream(fileName);
        workBookML.write(outputStream);
        controller.addToLog("Создан файл: " + fileName.toString());
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

            if (oldCell.getCellType() == Cell.CELL_TYPE_STRING) {
                newCell.setCellValue(oldCell.getStringCellValue());
            }

            if (oldCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                newCell.setCellValue(oldCell.getNumericCellValue());
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
