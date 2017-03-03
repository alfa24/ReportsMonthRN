package model;

import java.util.ArrayList;

/**
 * Created by Александр on 02.01.2017.
 * Класс хранит возможные категории работ
 * К каждой категории сопоставляется набор текстовых тегов tags (из конфига)
 */
public enum WorksCategory {
    VIDEO_MONITORING,
    HARDWARE_INSTALL,
    REMOTE,
    TO,
    DRIVE,
    NB,
    WEEKEND,
    STOP,
    FUELFLOW;

    ArrayList<String> tags = new ArrayList<>();

    //Проверяет есть ли в классе такое значение (Enum) как String category
    public static boolean isExistCategory(String category) {
        for (WorksCategory cat :
                WorksCategory.values()) {
            if (cat.toString().equals(category)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(String tags) {
        String[] tmp = tags.split(";");
        for (int i = 0; i < tmp.length; i++) {
            String s = tmp[i].trim();
            this.tags.add(s);
        }
    }


}
