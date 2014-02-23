package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";
    public MainPanel() {
        super(new GridLayout(3,1));

        Date date = new Date();
        JSpinner spinner1 = new JSpinner(new SpinnerDateModel(date, date, null, Calendar.DAY_OF_MONTH));
        spinner1.setEditor(new JSpinner.DateEditor(spinner1, DATE_FORMAT_PATTERN));

        Calendar today = Calendar.getInstance();
        today.clear(Calendar.MILLISECOND);
        today.clear(Calendar.SECOND);
        today.clear(Calendar.MINUTE);
        today.set(Calendar.HOUR_OF_DAY, 0);
        Date start = today.getTime();

        System.out.println(date);
        System.out.println(start);

        JSpinner spinner2 = new JSpinner(new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH));
        spinner2.setEditor(new JSpinner.DateEditor(spinner2, DATE_FORMAT_PATTERN));

        JSpinner spinner3 = new JSpinner(new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH));
        final JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner3, DATE_FORMAT_PATTERN);
        spinner3.setEditor(editor);
        editor.getTextField().addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        int i = DATE_FORMAT_PATTERN.lastIndexOf("dd");
                        editor.getTextField().select(i, i+2);
                    }
                });
            }
        });

        add(makeTitlePanel(spinner1, "Calendar.DAY_OF_MONTH"));
        add(makeTitlePanel(spinner2, "min: set(Calendar.HOUR_OF_DAY, 0)"));
        add(makeTitlePanel(spinner3, "JSpinner.DateEditor+FocusListener"));
        setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
