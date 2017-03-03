package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by ФаличевАЮ on 07.02.2017.
 */
public class ViewWindow extends JFrame {
    public static final int WIDTH_WINDOW = 500;
    public static final int HEIGHT_WINDOW = 500;
    private SettingWindow settingWindow;
    private Controller controller;
    private JTextArea logTextArea;
    private JButton startButton;
    private JTextField inFilePath;
    private JButton inFileButton;

    public ViewWindow(Controller controller) {
        this.controller = controller;
    }

    public void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH_WINDOW, HEIGHT_WINDOW);
        setTitle("Ежемесячная отчетность ОТП АЗК");
        setLocationRelativeTo(null);

        JMenuBar jMenuBar = new JMenuBar();
        setJMenuBar(jMenuBar);

        JMenu editMenu = new JMenu("Правка");
        JMenuItem settingMenuItem = new JMenuItem("Настройки");
        settingMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingWindow = new SettingWindow(controller);
                settingWindow.showWindow();
            }
        });
        editMenu.add(settingMenuItem);
        jMenuBar.add(editMenu);

        JMenu helpMenu = new JMenu("Помощь");
        JMenuItem helpMenuItem = new JMenuItem("Справка");
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(Paths.get("conf//help.doc").toFile());
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "Файл справки не найден.", "Ошибка открытия файла", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenuItem aboutMenuItem = new JMenuItem("О программе");
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(getParent(), "Версия программы: " + controller.getVersion(), "О программе", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(helpMenuItem);
        helpMenu.add(aboutMenuItem);
        jMenuBar.add(helpMenu);

        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(5);
        borderLayout.setVgap(5);
        setLayout(borderLayout);

        logTextArea = new JTextArea();
        logTextArea.setEnabled(false);
        logTextArea.setDisabledTextColor(Color.BLACK);
        logTextArea.setMargin(new Insets(5, 5, 5, 5));
        logTextArea.append("Выберите файл отчета по заявкам из ЦДС и нажмите <<Сформировать>> ..." + "\r\n");
        add(logTextArea);

        JPanel buttonPane = new JPanel();
        BoxLayout boxLayout = new BoxLayout(buttonPane, BoxLayout.X_AXIS);

        buttonPane.setLayout(boxLayout);
        inFilePath = new JTextField();
        buttonPane.add(inFilePath);
        buttonPane.add(Box.createRigidArea(new Dimension(5, 5)));

        JFileChooser jFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel 2003", "xls");
        jFileChooser.setFileFilter(filter);

        inFileButton = new JButton("...");
        inFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = jFileChooser.showOpenDialog(ViewWindow.this);
                if (i == JFileChooser.APPROVE_OPTION) {
                    inFilePath.setText(jFileChooser.getSelectedFile().toString());
                    inFilePath.updateUI();
                }
            }
        });
        buttonPane.add(inFileButton);

        buttonPane.add(Box.createRigidArea(new Dimension(5, 5)));
        startButton = new JButton("Сформировать");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inFilePath.getText().isEmpty()) {
                    controller.start(inFilePath.getText());
                } else {
                    JOptionPane.showMessageDialog(getParent(), "Не выбран Отчет по заявкам.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPane.add(startButton);

        add(buttonPane, BorderLayout.PAGE_END);
    }

    public void showWindow() {
        setVisible(true);
    }

    public void addToLog(String msg) {
        logTextArea.append(msg + "\r\n");
    }
}
