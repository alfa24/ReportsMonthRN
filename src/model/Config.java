package model;

import controller.Controller;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by Александр on 08.01.2017.
 */
public class Config {
    private ObjectInputStream conf;
    private String path_out_files;
    private int dateWinter;
    private int dateSummer;
    private ArrayList<String> azk_text_remove;
    private ArrayList<Driver> drivers;
    private HashMap<String, Double> distances;
    private TreeSet<Parametr> parametrs;
    private Controller controller;

    public Config() throws IOException {
        distances = new HashMap<>();
        azk_text_remove = new ArrayList<>();
        parametrs = new TreeSet<>();
    }

    //читаем конфиг
    public void read() throws IOException, ClassNotFoundException {
        String configPath = Constants.configPath;
        FileInputStream file;
        try {
            file = new FileInputStream(configPath);
        } catch (FileNotFoundException e) {
            createConfigFile();
            file = new FileInputStream(configPath);

        }
        conf = new ObjectInputStream(file);
        parametrs = (TreeSet<Parametr>) conf.readObject();
        conf.close();

        for (Parametr p :
                parametrs) {
            //заполняем значения категорий даннымы
            if (WorksCategory.isExistCategory(p.getParametr())) {
                WorksCategory cat = WorksCategory.valueOf(p.getParametr());
                cat.setTags(p.getValue());
            }

            if (p.getParametr().equals("DateWinter")) {
                dateWinter = Integer.parseInt(p.getValue());
            }

            if (p.getParametr().equals("DateSummer")) {
                dateSummer = Integer.parseInt(p.getValue());
            }

            if (p.getParametr().equals("path_out_files")) {
                path_out_files = p.getValue();
            }

            if (p.getParametr().equals("azk_text_remove")) {
                String[] s = p.getValue().split(";");
                for (int i = 0; i < s.length; i++) {
                    azk_text_remove.add(s[i].trim());
                }
            }
        }
        this.distances = parseDistances(Constants.pathTableDistances);
        this.drivers = parseDrivers(Constants.pathTableDrivers);
    }

    private HashMap<String, Double> parseDistances(String path_table_distances) throws IOException {
        HashMap<String, Double> distances = new HashMap<>();

        FileInputStream inputStream = new FileInputStream(path_table_distances);
        Workbook workBook = new HSSFWorkbook(inputStream);

        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) { //пропускаем шапку
            rowIterator.next();
        }

        int k = 1;
        while (rowIterator.hasNext()) {
            org.apache.poi.ss.usermodel.Row row = rowIterator.next();
            if (row.getLastCellNum() == 2) {
                try {
                    String azkNumber = row.getCell(0).toString().replace(".0", "").trim().toLowerCase();
//                    Integer dist = Integer.valueOf(row.getCell(1).toString().replace(".0", "").trim());
                    Double dist = row.getCell(1).getNumericCellValue();
                    distances.put(azkNumber, dist);
                } catch (IllegalStateException e) {
                    controller.addToLog("Ошибка! Размер компенсации не является числом в строке " + k);
                }
            }
            k++;
        }

        return distances;
    }

    private ArrayList<Driver> parseDrivers(String path_table) throws IOException {
        ArrayList<Driver> drivers = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(path_table);
        Workbook workBook = new HSSFWorkbook(inputStream);

        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) { //пропускаем шапку
            rowIterator.next();
        }

        int k = 1;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getLastCellNum() == 5) {
                try {
                    Driver driver = new Driver();
                    driver.setName(row.getCell(0).toString().trim());
                    driver.setAuto(row.getCell(1).toString().trim());
                    driver.setNumberAuto(row.getCell(2).toString().trim());
                    driver.setPayoutSummer(row.getCell(3).getNumericCellValue());
                    driver.setPayoutWinter(row.getCell(4).getNumericCellValue());
                    drivers.add(driver);
                } catch (IllegalStateException e) {
                    controller.addToLog("Ошибка! Размер компенсации не является числом в строке " + k);
                } catch (NullPointerException e) {
                    controller.addToLog("Ошибка! Строка № " + k + " Неверно заполнены данные в списке водителей. Данные по этому водителю игнорируются.");
                }
            }
            k++;
        }
        return drivers;
    }

    public ArrayList<String> getAzk_text_remove() {
        return azk_text_remove;
    }

    public String getPath_out_files() {
        return path_out_files;
    }

    public Driver getDriver(String nameDriver) {
        for (Driver driver :
                this.drivers) {
            if (driver.getName().equals(nameDriver)) {
                return driver;
            }
        }
        return null;
    }

    public Double getDistance(String azk) {
        Double result = 0.0;
        String number = azk.trim().toLowerCase();
        if (distances.containsKey(number)) {
            result = distances.get(number);
        } else {
            number = azk.substring(azk.indexOf(" "), azk.length()).trim().toLowerCase();
            if (distances.containsKey(number)) {
                result = distances.get(number);
            } else {
                controller.addToLog("Не найдено расстояние для " + azk + ".");
            }
        }
        return result;
    }

    public Double getPayout(String contractor, String mm) {
        Driver driver = getDriver(contractor);
        if (driver != null) {
            return driver.getPayout(mm);
        }
        return 0.0;
    }

    public TreeSet<Parametr> getParametrs() {
        return parametrs;
    }

    private void createConfigFile() throws IOException {
        int id = 0;

        Parametr parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("path_out_files");
        parametr.setDescription("Путь к каталогу, в котором будут создаваться отчеты.");
        parametr.setValue("\\Отчеты");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("azk_text_remove");
        parametr.setDescription("Лишние слова в колонке Местоположение.");
        parametr.setValue("Иркутскнефтепродукт, ЗАО.;(г. Ангарск); №");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("DateWinter");
        parametr.setDescription("месяц перехода на зимний тариф по топливу (включая этот месяц).");
        parametr.setValue("11");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("DateSummer");
        parametr.setDescription("месяц перехода на летний тариф по топливу (включая этот месяц).");
        parametr.setValue("5");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("VIDEO_MONITORING");
        parametr.setDescription("Работы по видеомонитрингу.");
        parametr.setValue("видео; !свн");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("DRIVE");
        parametr.setDescription("Работы с выездом на объект.");
        parametr.setValue("выезд");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("FUELFLOW");
        parametr.setDescription("Потребовался расход ГСМ.");
        parametr.setValue("!гсм; расходгсм");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("NB");
        parametr.setDescription("Заявки по нефтебазе.");
        parametr.setValue("нефтебаза");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("WEEKEND");
        parametr.setDescription("Работы в выходной.");
        parametr.setValue("выходной");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("STOP");
        parametr.setDescription("Был простой АЗК/АЗС.");
        parametr.setValue("простойАзк");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("HARDWARE_INSTALL");
        parametr.setDescription("Установка оборудования.");
        parametr.setValue("установка");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("REMOTE");
        parametr.setDescription("Работы выполненные удаленно.");
        parametr.setValue("удаленно");
        parametrs.add(parametr);

        parametr = new Parametr();
        id++;
        parametr.setId(id);
        parametr.setParametr("TO");
        parametr.setDescription("Тех. обслуживание.");
        parametr.setValue("!то");
        parametrs.add(parametr);

        saveParametrs(parametrs);
    }

    public void saveParametrs(TreeSet<Parametr> parametrs) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(Constants.configPath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(parametrs);
        objectOutputStream.close();
    }

    //строка параметра в конфиге
    public static class Parametr implements Serializable, Comparable<Parametr> {
        private static final long serialVersionUID = 1111575598422124441L;
        private int id;
        private String parametr;
        private String description;
        private String value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getParametr() {
            return parametr;
        }

        public void setParametr(String parametr) {
            this.parametr = parametr;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }


        @Override
        public int compareTo(Parametr o) {
            return this.id - o.id;
        }
    }

    //Класс хранит информацию о водителе
    public class Driver {
        private String name;
        private String auto;
        private String numberAuto;
        private Double payoutSummer;
        private Double payoutWinter;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAuto() {
            return auto;
        }

        public void setAuto(String auto) {
            this.auto = auto;
        }

        public String getNumberAuto() {
            return numberAuto;
        }

        public void setNumberAuto(String numberAuto) {
            this.numberAuto = numberAuto;
        }

        public Double getPayoutSummer() {
            return payoutSummer;
        }

        public void setPayoutSummer(Double payoutSummer) {
            this.payoutSummer = payoutSummer;
        }

        public Double getPayoutWinter() {
            return payoutWinter;
        }

        public void setPayoutWinter(Double payoutWinter) {
            this.payoutWinter = payoutWinter;
        }

        @Override
        public String toString() {
            return "Driver{" +
                    "name='" + name + '\'' +
                    ", auto='" + auto + '\'' +
                    ", numberAuto='" + numberAuto + '\'' +
                    ", payoutSummer=" + payoutSummer +
                    ", payoutWinter=" + payoutWinter +
                    '}';
        }

        public Double getPayout(String mm) {
            int currenMounth = Integer.valueOf(mm);
            if (currenMounth < dateSummer) {
                return payoutWinter;
            }

            if (currenMounth >= dateSummer & currenMounth < dateWinter) {
                return payoutSummer;
            }

            if (currenMounth >= dateWinter) {
                return payoutWinter;
            }

            return 0.0;
        }
    }

}
