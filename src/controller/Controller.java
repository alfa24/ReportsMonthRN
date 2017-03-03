package controller;

import model.Config;
import model.Constants;
import model.Report;
import view.ViewWindow;

import java.io.IOException;
import java.util.TreeSet;

/**
 * Created by ФаличевАЮ on 07.02.2017.
 */
public class Controller {
    private Report model;
    private ViewWindow view;

    public void showWindow() {
        view.showWindow();
    }

    public void init(Report model, ViewWindow view) {
        this.view = view;
        this.model = model;
    }

    public Config getConfig() {
        return model.getConfig();
    }

    public String getVersion() {
        return Constants.Version;
    }

    public void start(String path_report_works) {
        model.setPath_report_works(path_report_works);
        Thread thread = new Thread(model);
        thread.start();
    }

    public void addToLog(String msg) {
        view.addToLog(msg);
    }

    public void saveConfig(TreeSet<Config.Parametr> parametrs) throws IOException {
        model.getConfig().saveParametrs(parametrs);
    }
}
