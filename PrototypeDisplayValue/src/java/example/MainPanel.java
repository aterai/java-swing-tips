package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String TITLE = "MMMMMMMMMMMMMMMMMMMM";
    private final ComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[] {"a", "b", "c"});
    private final JComboBox<String> combo1 = new JComboBox<>(m);
    private final JComboBox<String> combo2 = new JComboBox<>(m);
    private final JComboBox<String> combo3 = new JComboBox<>(m);

    private final ComboBoxModel<Site> model = new DefaultComboBoxModel<>(new Site[] {
        new Site("a", new ColorIcon(Color.RED)),
        new Site("b", new ColorIcon(Color.GREEN)),
        new Site("c", new ColorIcon(Color.BLUE))});
    private final JComboBox<Site> combo4 = new JComboBox<>(model);
    private final JComboBox<Site> combo5 = new JComboBox<>(model);
    private final JComboBox<Site> combo6 = new JComboBox<>();

    public MainPanel() {
        super();
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        layout.putConstraint(SpringLayout.WEST, combo1,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo2,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo3,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo4,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo5,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo6,   10, SpringLayout.WEST,  this);

        layout.putConstraint(SpringLayout.NORTH, combo1,  10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.NORTH, combo2,  10, SpringLayout.SOUTH, combo1);
        layout.putConstraint(SpringLayout.NORTH, combo3,  10, SpringLayout.SOUTH, combo2);
        layout.putConstraint(SpringLayout.NORTH, combo4,  10, SpringLayout.SOUTH, combo3);
        layout.putConstraint(SpringLayout.NORTH, combo5,  10, SpringLayout.SOUTH, combo4);
        layout.putConstraint(SpringLayout.NORTH, combo6,  10, SpringLayout.SOUTH, combo5);

        //combo1.setEditable(true);
        //((JTextField) combo1.getEditor().getEditorComponent()).setColumns(20);

        combo2.setPrototypeDisplayValue(TITLE);
        combo3.setPrototypeDisplayValue(TITLE);
        combo3.setEditable(true);

        combo4.setRenderer(new SiteListCellRenderer<Site>());
        combo5.setRenderer(new SiteListCellRenderer<Site>());
        combo5.setPrototypeDisplayValue(new Site(TITLE, new ColorIcon(Color.GRAY)));
        combo6.setRenderer(new SiteListCellRenderer<Site>());
        combo6.setPrototypeDisplayValue(new Site(TITLE, new ColorIcon(Color.GRAY)));

        add(combo1);
        add(combo2);
        add(combo3);
        add(combo4);
        add(combo5);
        add(combo6);
        setPreferredSize(new Dimension(320, 240));
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

class Site {
    public final Icon favicon;
    public final String title;
    protected Site(String title, Icon favicon) {
        this.title = title;
        this.favicon = favicon;
    }
}

class ColorIcon implements Icon {
    private final Color color;
    protected ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.fillOval(4, 4, getIconWidth() - 8, getIconHeight() - 8);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 24;
    }
    @Override public int getIconHeight() {
        return 24;
    }
}

class SiteListCellRenderer<E extends Site> extends JLabel implements ListCellRenderer<E> {
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        setOpaque(index >= 0);
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        if (Objects.nonNull(value)) {
            setText(value.title);
            setIcon(value.favicon);
        }
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
