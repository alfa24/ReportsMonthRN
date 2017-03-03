package model;

import controller.Controller;

import java.io.IOException;


/**
 * Created by Александр on 02.01.2017.
 */
public class Report implements Runnable {

    private Config config;
    private Controller controller;
    private String path_report_works;

    ///////////////////////////////////////////////////////////////////////////
    // консрукторы и инициализация
    ///////////////////////////////////////////////////////////////////////////
    public Report(Controller controller) {
        this.controller = controller;
    }

    public void init() {
        try {
            config = new Config();
            config.read();
        } catch (Exception e) {
            controller.addToLog("Ошибка чтения файла настроек.");
        }
    }

    public void start() throws IOException {
        //Парсим отчет по заявкам из HelpDesk
        ReportWorks reportWorks = new ReportWorks(config);
        controller.addToLog("Считываем отчет по заявкам...");
        reportWorks.parse(controller, path_report_works);

        //Формируем отчет по выполненным работам
        controller.addToLog("Формируем ежемесячный отчет...");
        MonthlyReport monthlyReport = new MonthlyReport(reportWorks, config, controller);
        monthlyReport.create(config.getPath_out_files());

        //Формируем отчет по маршрутным листам
        controller.addToLog("Формируем отчет по ГСМ...");
        ReportFuelFlow reportFuelFlow = new ReportFuelFlow(reportWorks, config, controller);
        reportFuelFlow.create();
        controller.addToLog("Обработка завершена!");
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            controller.addToLog("Ошибка запуска программы.");
        }
    }

    public void setPath_report_works(String path_report_works) {
        this.path_report_works = path_report_works;
    }
}
