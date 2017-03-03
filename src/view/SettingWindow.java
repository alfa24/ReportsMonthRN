package view;

import controller.Controller;
import model.Config;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Created by ФаличевАЮ on 07.02.2017.
 */
public class SettingWindow extends JFrame {
    public static final int WIDTH_WINDOW = 800;
    public static final int HEIGHT_WINDOW = 500;
    private Controller controller;
    private TreeSet<Config.Parametr> parametrs;

    public SettingWindow(Controller controller) {
        this.controller = controller;
        HashMap<Config.Parametr, JTextField> fields = new HashMap<>();
        parametrs = controller.getConfig().getParametrs();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(WIDTH_WINDOW, HEIGHT_WINDOW);
        setTitle("Настройки");
        setLocationRelativeTo(null);

        //Таблица с настройками
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        Vector<String> header = new Vector<>();
        header.add("№");
        header.add("Параметр");
        header.add("Значение");

        for (Config.Parametr p :
                parametrs) {
            Vector<String> row = new Vector<>();
            row.add(String.valueOf(p.getId()));
            row.add(p.getDescription());
            row.add(p.getValue());
            data.add(row);
        }
        JTable jTable = new JTable(data, header) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column <= 1) {
                    return false;
                }
                return super.isCellEditable(row, column);
            }
        };
        TableColumn col = jTable.getColumnModel().getColumn(0);
        col.setMaxWidth(30);

        jTable.setRowHeight(20);
        jTable.setGridColor(Color.blue);
        jTable.setShowVerticalLines(false);


        add(BorderLayout.CENTER, new JScrollPane(jTable));
        JPanel buttonPane = new JPanel();
        BoxLayout boxLayout = new BoxLayout(buttonPane, BoxLayout.X_AXIS);

        buttonPane.setLayout(boxLayout);
        buttonPane.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton saveeButton = new JButton("Сохранить");
        saveeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfig(data);
            }
        });
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowsClosing();
            }
        });
        buttonPane.add(saveeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(5, 5)));
        buttonPane.add(closeButton);
        add(BorderLayout.PAGE_END, buttonPane);
    }

    private void saveConfig(Vector<Vector<String>> data) {
        for (Config.Parametr p :
                parametrs) {
            int id = p.getId();
            Vector<String> row = data.get(id - 1);
            p.setValue(row.get(2));
        }
        try {
            controller.saveConfig(parametrs);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(getParent(), "Не удалось сохранить настройки, возможно файл открыт в другой программе.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }

    public void showWindow() {
        setVisible(true);
    }

    public void windowsClosing() {
        dispose();
    }

}
