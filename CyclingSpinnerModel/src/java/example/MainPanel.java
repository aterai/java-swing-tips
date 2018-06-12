package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(2, 1));

        List<String> weeks = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Sat");

        JSpinner spinner01 = new JSpinner();
        spinner01.setModel(new SpinnerNumberModel(20, 0, 59, 1));

        JSpinner spinner02 = new JSpinner();
        spinner02.setModel(new SpinnerListModel(weeks));

        JSpinner spinner03 = new JSpinner();
        spinner03.setModel(new SpinnerNumberModel(20, 0, 59, 1) {
            @Override public Object getNextValue() {
                Object n = super.getNextValue();
                return Objects.nonNull(n) ? n : getMinimum();
            }
            @Override public Object getPreviousValue() {
                Object n = super.getPreviousValue();
                return Objects.nonNull(n) ? n : getMaximum();
            }
        });

        JSpinner spinner04 = new JSpinner();
        spinner04.setModel(new SpinnerListModel(weeks) {
            @Override public Object getNextValue() {
                Object o = super.getNextValue();
                return Objects.nonNull(o) ? o : getList().get(0);
            }
            @Override public Object getPreviousValue() {
                List<?> l = getList();
                Object o = super.getPreviousValue();
                return Objects.nonNull(o) ? o : l.get(l.size() - 1);
            }
        });
        add(makeTitledPanel("default model", Arrays.asList(spinner01, spinner02)));
        add(makeTitledPanel("cycling model", Arrays.asList(spinner03, spinner04)));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, List<? extends Component> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1d;
        c.gridx = GridBagConstraints.REMAINDER;
        for (Component cmp: list) {
            p.add(cmp, c);
        }
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
