package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Александр on 02.01.2017.
 * Хранит данные строки Excel
 */
public class Works {
    private String number;
    private Date date;
    private String azk;
    private String contractor;
    private ArrayList<WorksCategory> worksCategories;

    public Works() {
        worksCategories = new ArrayList<>();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDate(String format) {
        if (format != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } else {
            return date.toString();
        }
    }

    public String getAzk() {
        return azk;
    }

    public void setAzk(String azk) {
        this.azk = azk.replace("  ", " ");
    }

    //если работа принадлежит категории то возвращаем Истина
    public boolean isIncludeCategory(WorksCategory category) {
        for (WorksCategory workCategory :
                worksCategories) {
            if (workCategory == category) {
                return true;
            }
        }
        return false;
    }

    public void addCategory(WorksCategory cat) {
        worksCategories.add(cat);
    }

    public String getContractor() {
        return contractor;
    }

    public void setContractor(String contractor) {
        this.contractor = contractor;
    }
}
