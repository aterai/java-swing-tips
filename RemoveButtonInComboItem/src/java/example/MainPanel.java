package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(2, 1));
        final JComboBox<String> c0 = makeComboBox(true,  false);
        final JComboBox<String> c1 = makeComboBox(false, false);
        final JComboBox<String> c2 = makeComboBox(true,  true);
        final JComboBox<String> c3 = makeComboBox(false, true);

        p.add(makeTitlePanel("setEditable(false)", Arrays.asList(c0, c1)));
        p.add(makeTitlePanel("setEditable(true)",  Arrays.asList(c2, c3)));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(p, BorderLayout.NORTH);
        add(new JButton(new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                String str = new Date().toString();
                for (JComboBox<String> c: Arrays.asList(c0, c1, c2, c3)) {
                    MutableComboBoxModel<String> m = (MutableComboBoxModel<String>) c.getModel();
                    m.insertElementAt(str, m.getSize());
                }
            }
        }), BorderLayout.SOUTH);
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
    private JComponent makeTitlePanel(String title, List<? extends JComponent> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1d;
        c.gridx   = GridBagConstraints.REMAINDER;
        for (JComponent cmp: list) {
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
    protected RemoveButtonComboBox(ComboBoxModel<E> aModel) {
        super(aModel);
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
    protected Optional<? extends JList> getList() {
        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof BasicComboPopup) {
            return Optional.of(((BasicComboPopup) a).getList());
        }
        return Optional.empty();
    }
}

class CellButtonsMouseListener extends MouseAdapter {
    private int prevIndex = -1;
    private JButton prevButton;
    private static void listRepaint(JList list, Rectangle rect) {
        if (Objects.nonNull(rect)) {
            list.repaint(rect);
        }
    }
    @Override public void mouseMoved(MouseEvent e) {
        JList list = (JList) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (!list.getCellBounds(index, index).contains(pt)) {
            if (prevIndex >= 0) {
                Rectangle r = list.getCellBounds(prevIndex, prevIndex);
                listRepaint(list, r);
            }
            index = -1;
            prevButton = null;
            return;
        }
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
            if (Objects.nonNull(button)) {
                renderer.rolloverIndex = index;
                if (!button.equals(prevButton)) {
                    Rectangle r = list.getCellBounds(prevIndex, index);
                    listRepaint(list, r);
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
                listRepaint(list, r);
                prevIndex = -1;
            }
            prevButton = button;
        }
        prevIndex = index;
    }
    @Override public void mousePressed(MouseEvent e) {
        JList list = (JList) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (Objects.nonNull(button)) {
                listRepaint(list, list.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        JList list = (JList) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (Objects.nonNull(button)) {
                button.doClick();
                Rectangle r = list.getCellBounds(index, index);
                listRepaint(list, r);
            }
        }
    }
    @Override public void mouseExited(MouseEvent e) {
        JList list = (JList) e.getComponent();
        ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
        renderer.rolloverIndex = -1;
    }
    @SuppressWarnings("unchecked")
    private static JButton getButton(JList list, Point pt, int index) {
        Container c = (Container) list.getCellRenderer().getListCellRendererComponent(list, "", index, false, false);
        Rectangle r = list.getCellBounds(index, index);
        c.setBounds(r);
        //c.doLayout(); //may be needed for mone LayoutManager
        pt.translate(-r.x, -r.y);
        Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
        if (b instanceof JButton) {
            return (JButton) b;
        } else {
            return null;
        }
    }
}

class ButtonsRenderer<E> extends JPanel implements ListCellRenderer<E> {
    private static final Color EVEN_COLOR = new Color(230, 255, 230);
    protected final RemoveButtonComboBox<E> comboBox;
    protected JList list;
    protected int index;
    public int rolloverIndex = -1;
    private final JLabel label = new DefaultListCellRenderer();
    private final JButton deleteButton = new JButton(new AbstractAction("x") {
        @Override public void actionPerformed(ActionEvent e) {
            MutableComboBoxModel m = (MutableComboBoxModel) list.getModel();
            boolean isMoreThanOneItem = m.getSize() > 1;
            if (isMoreThanOneItem) {
                m.removeElementAt(index);
                comboBox.showPopup();
            }
        }
    }) {
        @Override public Dimension getPreferredSize() {
            return new Dimension(16, 16);
        }
    };
    protected ButtonsRenderer(RemoveButtonComboBox<E> comboBox) {
        super(new BorderLayout());
        this.comboBox = comboBox;
        label.setOpaque(false);
        setOpaque(true);
        add(label);
        deleteButton.setBorder(BorderFactory.createEmptyBorder());
        deleteButton.setFocusable(false);
        deleteButton.setRolloverEnabled(false);
        deleteButton.setContentAreaFilled(false);
        add(deleteButton, BorderLayout.EAST);
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(Objects.toString(value, ""));
        this.list = list;
        this.index = index;
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            setBackground(index % 2 == 0 ? EVEN_COLOR : list.getBackground());
            label.setForeground(list.getForeground());
        }
        MutableComboBoxModel m = (MutableComboBoxModel) list.getModel();
        if (index < 0 || m.getSize() - 1 <= 0) {
            setOpaque(false);
            deleteButton.setVisible(false);
            label.setForeground(list.getForeground());
        } else {
            boolean f = index == rolloverIndex;
            setOpaque(true);
            deleteButton.setVisible(true);
            deleteButton.getModel().setRollover(f);
            deleteButton.setForeground(f ? Color.WHITE : list.getForeground());
        }
        return this;
    }
}
