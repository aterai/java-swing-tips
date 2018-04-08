package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JComboBox<String> c0 = makeComboBox(true, false);
        JComboBox<String> c1 = makeComboBox(false, false);
        JComboBox<String> c2 = makeComboBox(true, true);
        JComboBox<String> c3 = makeComboBox(false, true);

        JButton button = new JButton("add");
        button.addActionListener(e -> {
            String str = LocalDateTime.now().toString();
            for (JComboBox<String> c: Arrays.asList(c0, c1, c2, c3)) {
                MutableComboBoxModel<String> m = (MutableComboBoxModel<String>) c.getModel();
                m.insertElementAt(str, m.getSize());
            }
        });

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(makeTitledPanel("setEditable(false)", Arrays.asList(c0, c1)));
        p.add(makeTitledPanel("setEditable(true)", Arrays.asList(c2, c3)));

        add(p, BorderLayout.NORTH);
        add(button, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComboBox<String> makeComboBox(boolean isDefault, boolean isEditable) {
        ComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[] {"aaa", "bbb", "ccc"});
        JComboBox<String> comboBox;
        if (isDefault) {
            comboBox = new JComboBox<>(m);
        } else {
            comboBox = new RemoveButtonComboBox<>(m);
        }
        comboBox.setEditable(isEditable);
        return comboBox;
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

class RemoveButtonComboBox<E> extends JComboBox<E> {
    private transient CellButtonsMouseListener cbml;
    protected RemoveButtonComboBox(ComboBoxModel<E> model) {
        super(model);
    }
    @Override public void updateUI() {
        if (Objects.nonNull(cbml)) {
            getList().ifPresent(list -> {
                list.removeMouseListener(cbml);
                list.removeMouseMotionListener(cbml);
            });
        }
        super.updateUI();
        setRenderer(new ButtonsRenderer<>(this));
        getList().ifPresent(list -> {
            cbml = new CellButtonsMouseListener();
            list.addMouseListener(cbml);
            list.addMouseMotionListener(cbml);
        });
    }
    protected Optional<JList<?>> getList() {
        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof ComboPopup) {
            return Optional.of(((ComboPopup) a).getList());
        }
        return Optional.empty();
    }
}

class CellButtonsMouseListener extends MouseAdapter {
    private int prevIndex = -1;
    private JButton prevButton;
    private static void rectRepaint(JComponent c, Rectangle rect) {
        Optional.ofNullable(rect).ifPresent(c::repaint);
    }
    @Override public void mouseMoved(MouseEvent e) {
        JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (!list.getCellBounds(index, index).contains(pt)) {
            if (prevIndex >= 0) {
                Rectangle r = list.getCellBounds(prevIndex, prevIndex);
                rectRepaint(list, r);
            }
            index = -1;
            prevButton = null;
            return;
        }
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            ButtonsRenderer<?> renderer = (ButtonsRenderer<?>) list.getCellRenderer();
            if (Objects.nonNull(button)) {
                renderer.rolloverIndex = index;
                if (!button.equals(prevButton)) {
                    Rectangle r = list.getCellBounds(prevIndex, index);
                    rectRepaint(list, r);
                }
            } else {
                renderer.rolloverIndex = -1;
                Rectangle r = null;
                if (prevIndex == index) {
                    if (prevIndex >= 0 && Objects.nonNull(prevButton)) {
                        r = list.getCellBounds(prevIndex, prevIndex);
                    }
                } else {
                    r = list.getCellBounds(index, index);
                }
                rectRepaint(list, r);
                prevIndex = -1;
            }
            prevButton = button;
        }
        prevIndex = index;
    }
    @Override public void mousePressed(MouseEvent e) {
        JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (Objects.nonNull(button)) {
                rectRepaint(list, list.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (Objects.nonNull(button)) {
                button.doClick();
                Rectangle r = list.getCellBounds(index, index);
                rectRepaint(list, r);
            }
        }
    }
    @Override public void mouseExited(MouseEvent e) {
        JList<?> list = (JList<?>) e.getComponent();
        ((ButtonsRenderer<?>) list.getCellRenderer()).rolloverIndex = -1;
    }
    private static <E> JButton getButton(JList<E> list, Point pt, int index) {
        Component c = list.getCellRenderer().getListCellRendererComponent(list, list.getPrototypeCellValue(), index, false, false);
        Rectangle r = list.getCellBounds(index, index);
        c.setBounds(r);
        // c.doLayout(); // may be needed for mone LayoutManager
        pt.translate(-r.x, -r.y);
        Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
        if (b instanceof JButton) {
            return (JButton) b;
        } else {
            return null;
        }
    }
}

class ButtonsRenderer<E> implements ListCellRenderer<E> {
    private static final Color EVEN_COLOR = new Color(230, 255, 230);
    protected int targetIndex;
    protected int rolloverIndex = -1;
    private final JPanel panel = new JPanel(new BorderLayout()) {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = 0;
            return d;
        }
    };
    private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
    private final JButton deleteButton = new JButton("x") {
        @Override public Dimension getPreferredSize() {
            return new Dimension(16, 16);
        }
        @Override public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createEmptyBorder());
            setFocusable(false);
            setRolloverEnabled(false);
            setContentAreaFilled(false);
        }
    };
    protected ButtonsRenderer(RemoveButtonComboBox<E> comboBox) {
        deleteButton.addActionListener(e -> {
            ComboBoxModel<E> m = comboBox.getModel();
            boolean isMoreThanOneItem = m.getSize() > 1;
            if (isMoreThanOneItem && m instanceof MutableComboBoxModel) {
                ((MutableComboBoxModel<?>) m).removeElementAt(targetIndex);
                comboBox.showPopup();
            }
        });
        panel.setOpaque(true);
        panel.add(deleteButton, BorderLayout.EAST);
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (index < 0) {
            return l;
        }
        l.setOpaque(false);
        this.targetIndex = index;
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
        } else {
            panel.setBackground(index % 2 == 0 ? EVEN_COLOR : list.getBackground());
        }
        boolean showDeleteButton = list.getModel().getSize() > 1;
        if (showDeleteButton) {
            boolean isRollover = index == rolloverIndex;
            deleteButton.setVisible(true);
            deleteButton.getModel().setRollover(isRollover);
            deleteButton.setForeground(isRollover ? Color.WHITE : list.getForeground());
        } else {
            deleteButton.setVisible(false);
        }
        panel.add(l);
        return panel;
    }
}
