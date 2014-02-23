package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private static final int STEP   = 5;
    private static final int EXTENT = 20;
    private static final int MIN    = 0;
    private static final int MAX    = EXTENT*10; //200
    private static final int VALUE  = 50;
    private final JSpinner spinner;
    private final JScrollBar scrollbar;

    public MainPanel() {
        super(new GridLayout(2,1));
        scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, VALUE, EXTENT, MIN, MAX+EXTENT);
        scrollbar.setUnitIncrement(STEP);
        scrollbar.getModel().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                BoundedRangeModel m = (BoundedRangeModel)e.getSource();
                spinner.setValue(m.getValue());
            }
        });

        spinner = new JSpinner(new SpinnerNumberModel(VALUE, MIN, MAX, STEP));
        spinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JSpinner source = (JSpinner)e.getSource();
                Integer iv = (Integer)source.getValue();
                scrollbar.setValue(iv);
            }
        });

        add(makeTitlePanel(spinner, "JSpinner"));
        add(makeTitlePanel(scrollbar, "JScrollBar"));
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
