package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextArea log = new JTextArea();

        JButton button1 = new JButton("Default");
        button1.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int retvalue = chooser.showOpenDialog(log.getRootPane());
            if (retvalue == JFileChooser.APPROVE_OPTION) {
                log.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton button2 = new JButton("LayoutOrientation: VERTICAL");
        button2.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            stream(chooser)
              .filter(JList.class::isInstance)
              .map(JList.class::cast)
              .findFirst()
              .ifPresent(MainPanel::addHierarchyListener);
            int retvalue = chooser.showOpenDialog(log.getRootPane());
            if (retvalue == JFileChooser.APPROVE_OPTION) {
                log.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
        p.add(button1);
        p.add(button2);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
        setPreferredSize(new Dimension(320, 240));
    }
    // https://github.com/aterai/java-swing-tips/blob/master/GetComponentsRecursively/src/java/example/MainPanel.java
    public static Stream<Component> stream(Container parent) {
        return Arrays.stream(parent.getComponents())
            .filter(Container.class::isInstance)
            .map(c -> stream(Container.class.cast(c)))
            .reduce(Stream.of(parent), Stream::concat);
    }
    private static void addHierarchyListener(JList<?> list) {
        list.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && e.getComponent().isShowing()) {
                    list.putClientProperty("List.isFileList", Boolean.FALSE);
                    list.setLayoutOrientation(JList.VERTICAL);
                }
            }
        });
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
