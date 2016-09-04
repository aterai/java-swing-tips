package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final transient ListItem[] defaultModel = {
        new ListItem("wi0009-32.png"),
        new ListItem("wi0054-32.png"),
        new ListItem("wi0062-32.png"),
        new ListItem("wi0063-32.png"),
        new ListItem("wi0064-32.png"),
        new ListItem("wi0096-32.png"),
        new ListItem("wi0111-32.png"),
        new ListItem("wi0122-32.png"),
        new ListItem("wi0124-32.png"),
        new ListItem("wi0126-32.png")
    };
    private final DefaultListModel<ListItem> model = new DefaultListModel<>();
    private final JList<ListItem> list = new JList<ListItem>(model) {
        @Override public void updateUI() {
            setSelectionForeground(null); //Nimbus
            setSelectionBackground(null); //Nimbus
            setCellRenderer(null);
            super.updateUI();
            setLayoutOrientation(JList.HORIZONTAL_WRAP);
            setVisibleRowCount(0);
            setFixedCellWidth(82);
            setFixedCellHeight(64);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setCellRenderer(new ListItemListCellRenderer<ListItem>());
            getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    };
    private final JTextField field = new JTextField(15);

    private MainPanel() {
        super(new BorderLayout(5, 5));

        for (ListItem item: defaultModel) {
            model.addElement(item);
        }

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                filter();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                filter();
            }
            @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
        });

        add(field, BorderLayout.NORTH);
        add(new JScrollPane(list));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private Optional<Pattern> getPattern() {
        String pattern = field.getText();
        if (Objects.isNull(pattern) || pattern.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Pattern.compile(pattern));
        } catch (PatternSyntaxException ex) {
            return Optional.empty();
        }
    }
    private void filter() {
        getPattern().ifPresent(pattern -> {
            for (ListItem item: defaultModel) {
                if (pattern.matcher(item.title).find()) {
                    if (!model.contains(item)) {
                        model.addElement(item);
                    }
                } else {
                    model.removeElement(item);
                }
//                 if (!pattern.matcher(item.title).find()) {
//                     model.removeElement(item);
//                 } else if (!model.contains(item)) {
//                     model.addElement(item);
//                 }
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

class ListItem {
    public final ImageIcon nicon;
    public final ImageIcon sicon;
    public final String title;
    protected ListItem(String iconfile) {
        this.nicon = new ImageIcon(getClass().getResource(iconfile));
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
        this.sicon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
        this.title = iconfile;
    }
}

class SelectedImageFilter extends RGBImageFilter {
    @Override public int filterRGB(int x, int y, int argb) {
        return (argb & 0xFFFFFF00) | ((argb & 0xFF) >> 1);
    }
}

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon  = new JLabel((Icon) null, SwingConstants.CENTER);
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final Border focusCellHighlightBorder = UIManager.getBorder("List.focusCellHighlightBorder");
    private final Border noFocusBorder;

    protected ListItemListCellRenderer() {
        Border b = UIManager.getBorder("List.noFocusBorder");
        if (Objects.isNull(b)) { //Nimbus???
            Insets i = focusCellHighlightBorder.getBorderInsets(label);
            b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
        }
        noFocusBorder = b;
        icon.setOpaque(false);
        label.setForeground(p.getForeground());
        label.setBackground(p.getBackground());
        label.setBorder(noFocusBorder);
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        p.add(icon);
        p.add(label, BorderLayout.SOUTH);
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(value.title);
        label.setBorder(cellHasFocus ? focusCellHighlightBorder : noFocusBorder);
        if (isSelected) {
            icon.setIcon(value.sicon);
            label.setForeground(list.getSelectionForeground());
            label.setBackground(list.getSelectionBackground());
            label.setOpaque(true);
        } else {
            icon.setIcon(value.nicon);
            label.setForeground(list.getForeground());
            label.setBackground(list.getBackground());
            label.setOpaque(false);
        }
        return p;
    }
}
