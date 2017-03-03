import controller.Controller;
import model.Report;
import view.ViewWindow;

/**
 * Created by Александр on 02.01.2017.
 */
public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        Report report = new Report(controller);
        ViewWindow viewWindow = new ViewWindow(controller);


        viewWindow.init();
        controller.init(report, viewWindow);
        controller.showWindow();
        report.init();

    }
}
