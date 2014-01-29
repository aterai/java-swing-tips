package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final ColorItem[] model = new ColorItem[] {
        new ColorItem(Color.RED,     "Red"),
        new ColorItem(Color.GREEN,   "Green"),
        new ColorItem(Color.BLUE,    "Blue"),
        new ColorItem(Color.CYAN,    "Cyan"),
        new ColorItem(Color.ORANGE,  "Orange"),
        new ColorItem(Color.MAGENTA, "Magenta"),
    };
    private final JComboBox<ColorItem> combo00 = new JComboBox<>(model);
    private final JComboBox<ColorItem> combo01 = new JComboBox<>(model);
    private final JComboBox<ColorItem> combo02 = new JComboBox<>(model);

    public MainPanel() {
        super(new BorderLayout());
        combo01.setRenderer(new ComboForegroundRenderer(combo01));
        combo02.setRenderer(new ComboHtmlRenderer());

        Box box = Box.createVerticalBox();
        box.add(createPanel(combo00, "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo01, "setForeground:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo02, "html tag:"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        return panel;
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

class ColorItem {
    public final Color color;
    public final String description;
    public ColorItem(Color color, String description) {
        this.color = color;
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
}

class ComboForegroundRenderer extends DefaultListCellRenderer {
    private final Color SELECTION_BACKGROUND = new Color(240,245,250);
    private final JComboBox combo;
    public ComboForegroundRenderer(JComboBox combo) {
        super();
        this.combo = combo;
    }
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        if(value instanceof ColorItem) {
            ColorItem item = (ColorItem) value;
            Color ic = item.color;
            if(index<0 && ic!=null && !ic.equals(combo.getForeground())) {
                combo.setForeground(ic); //Windows, Motif Look&Feel
                list.setSelectionForeground(ic);
                list.setSelectionBackground(SELECTION_BACKGROUND);
            }
            JLabel l = (JLabel)super.getListCellRendererComponent(list, item.description, index, isSelected, hasFocus);
            l.setForeground(ic);
            l.setBackground(isSelected ? SELECTION_BACKGROUND : list.getBackground());
            //l.setText(item.description);
            return l;
        }else{
            super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            return this;
        }
    }
}

class ComboHtmlRenderer extends DefaultListCellRenderer {
    private static final Color SELECTION_BACKGROUND = new Color(240,245,250);
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        ColorItem item = (ColorItem)value;
        if(index<0) {
            list.setSelectionBackground(SELECTION_BACKGROUND);
        }
        JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        l.setText("<html><font color="+hex(item.color)+">"+item.description);
        l.setBackground(isSelected ? SELECTION_BACKGROUND : list.getBackground());
        return l;
    }
    private static String hex(Color c) {
        return String.format("#%06x", c.getRGB()&0xffffff);
    }
}
