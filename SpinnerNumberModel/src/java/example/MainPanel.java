package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Arrays;
import javax.swing.*;

public class MainPanel extends JPanel {
    List<SpinnerNumberModel> list1 = Arrays.asList(
        new SpinnerNumberModel(Byte.valueOf((byte)Byte.MAX_VALUE),
                               Byte.valueOf((byte)0),
                               Byte.valueOf((byte)Byte.MAX_VALUE),
                               Byte.valueOf((byte)1)),
        new SpinnerNumberModel(Short.valueOf((short)Short.MAX_VALUE),
                               Short.valueOf((short)0),
                               Short.valueOf((short)Short.MAX_VALUE),
                               Short.valueOf((short)1)),
        new SpinnerNumberModel(Integer.MAX_VALUE,
                               0,
                               Integer.MAX_VALUE,
                               1),
        new SpinnerNumberModel(Long.valueOf(Long.MAX_VALUE),
                               Long.valueOf(0),
                               Long.valueOf(Long.MAX_VALUE),
                               Long.valueOf(1))
        );
    List<SpinnerNumberModel> list2 = Arrays.asList(
        new SpinnerNumberModel(Long.valueOf(Byte.MAX_VALUE),
                               Long.valueOf(0),
                               Long.valueOf(Byte.MAX_VALUE),
                               Long.valueOf(1)),
        new SpinnerNumberModel(Long.valueOf(Short.MAX_VALUE),
                               Long.valueOf(0),
                               Long.valueOf(Short.MAX_VALUE),
                               Long.valueOf(1)),
        new SpinnerNumberModel(Long.valueOf(Integer.MAX_VALUE),
                               Long.valueOf(0),
                               Long.valueOf(Integer.MAX_VALUE),
                               Long.valueOf(1)),
        new SpinnerNumberModel(Long.valueOf(Long.MAX_VALUE),
                               Long.valueOf(0),
                               Long.valueOf(Long.MAX_VALUE),
                               Long.valueOf(1))
        );

    public MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(makePanel("Byte, Short, Integer, Long", makeJSpinnerListPanel(list1)));
        box.add(makePanel("Long.valueOf",               makeJSpinnerListPanel(list2)));
        box.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Box makeJSpinnerListPanel(List<SpinnerNumberModel> list) {
        Box box = Box.createVerticalBox();
        for(SpinnerNumberModel m: list) {
            box.add(new JSpinner(m));
            box.add(Box.createVerticalStrut(2));
        }
        return box;
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
