package model;

import java.nio.file.Paths;

/**
 * Created by ФаличевАЮ on 07.02.2017.
 */
public class Constants {
    public static final String Version = "1.2";
    public static final String configPath = Paths.get("conf//Config.bin").toString();
    public static final String tmplReportGSM = Paths.get("tmpl//tmplReportGSM.xls").toString();
    public static final String tmplML = Paths.get("tmpl//tmplML.xls").toString();
    public static final String templateMonthReportsPath = Paths.get("tmpl//tmplReportWorks.xls").toString();
    public static final String pathTableDistances = Paths.get("conf//Таблица расстояний.xls").toString();
    public static final String pathTableDrivers = Paths.get("conf//Водители.xls").toString();
}
