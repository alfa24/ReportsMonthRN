package exceptions;

/**
 * Created by ФаличевАЮ on 24.01.2017.
 */
public class DriverNotFoundExceptions extends Exception {

    public DriverNotFoundExceptions(String s) {
        super(s);
    }

    @Override
    public String getLocalizedMessage() {
        return "Не найден сотрудник в списке водителей.";
    }
}
