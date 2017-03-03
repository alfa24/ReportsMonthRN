package model;

import controller.Controller;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


/**
 * Created by Александр on 02.01.2017.
 */
public class ReportWorks {
    private Config config;
    private ArrayList<Works> dataBase;

    public ReportWorks(Config config) {
        this.config = config;
        dataBase = new ArrayList<>();
    }

    //считываем входной файл, анализируем тэги и в соответствии с этим указываем категорию к которой относится работа
    public void parse(Controller controller, String path_report_works) throws IOException {
        ArrayList<String> azkTextRemove = config.getAzk_text_remove();
        FileInputStream inputStream = new FileInputStream(path_report_works);
        Workbook workBook = new HSSFWorkbook(inputStream);

        Sheet sheet = workBook.getSheetAt(0);
        Iterator<org.apache.poi.ss.usermodel.Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) { //пропускаем шапку
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            org.apache.poi.ss.usermodel.Row row = rowIterator.next();
            Works work = new Works();
            work.setNumber(row.getCell(0).toString().replace(".0", ""));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date d = sdf.parse(row.getCell(1).toString());
                sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                d = sdf.parse(sdf.format(d));
                work.setDate(d);
            } catch (ParseException e) {
                work.setDate(null);
            }

            String azkName = row.getCell(5).toString();
            for (String s :
                    azkTextRemove) {
                azkName = azkName.replace(s, "");
            }
            work.setAzk(azkName.trim());
            work.setContractor(row.getCell(10).toString());
            String comment = row.getCell(16).toString();
            for (WorksCategory cat :
                    WorksCategory.values()) {
                ArrayList<String> tags = cat.getTags();
                for (String s :
                        tags) {
                    if (comment.contains(s)) {
                        work.addCategory(cat);
                    }
                }
            }
            dataBase.add(work);
        }
    }

    //количество записей в базе
    public int getCountWorks() {
        return dataBase.size();
    }

    //Фильтрует и фозвращает список записей согласно фильтру category
    public ArrayList<Works> getWorks(WorksCategory category) {
        ArrayList<Works> result = new ArrayList<>();
        for (Works work :
                dataBase) {
            if (work.isIncludeCategory(category)) {
                result.add(work);
            }
        }
        return result;
    }

    //возвращает месяц и год в формате "MM.yyyy", за который формируется отчет. Т.К. по заданию заведомо
    //входной файл содержит работы за 1 месяц, то возвращаем значение из первой попавшейся записи
    public String getPeriod(String format) {
        for (Works work :
                dataBase) {
            return work.getDate(format);
        }
        return null;
    }

    public ArrayList<String> getContractors() {
        ArrayList<String> result = new ArrayList<>();
        for (Works work :
                dataBase) {
            String contractor = work.getContractor();
            if (!result.contains(contractor)) {
                result.add(contractor);
            }
        }
        return result;
    }

    public ArrayList<Works> getWorks(String contractor, WorksCategory category) {
        ArrayList<Works> result = new ArrayList<>();
        for (Works work :
                dataBase) {
            if (work.isIncludeCategory(category) & work.getContractor().equals(contractor)) {
                result.add(work);
            }
        }
        return result;
    }

    public String getFirstDate(String format) {
        for (Works work :
                dataBase) {
            return work.getDate(format);
        }
        return null;
    }
}
