package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.accessibility.Accessible;
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
    private static JComboBox<String> makeComboBox(final boolean isDefault, boolean isEditable) {
        String[] m = {"aaa", "bbb", "ccc"};
        JComboBox<String> comboBox = new JComboBox<String>(m) {
            @Override public void updateUI() {
                super.updateUI();
                if (isDefault) {
                    return;
                }
                setRenderer(new ButtonsRenderer(this));
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof BasicComboPopup) {
                    BasicComboPopup pop = (BasicComboPopup) a;
                    JList list = pop.getList();
                    CellButtonsMouseListener cbml = new CellButtonsMouseListener();
                    list.addMouseListener(cbml);
                    list.addMouseMotionListener(cbml);
                }
            }
        };
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
        c.gridy   = 0;
        for (JComponent cmp: list) {
            p.add(cmp, c);
            c.gridy++;
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

class CellButtonsMouseListener extends MouseAdapter {
    private int prevIndex = -1;
    private JButton prevButton;
    private static void listRepaint(JList list, Rectangle rect) {
        if (rect != null) {
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
            renderer.button = button;
            if (button == null) {
                renderer.rolloverIndex = -1;
                Rectangle r = null;
                if (prevIndex == index) {
                    if (prevIndex >= 0 && prevButton != null) {
                        r = list.getCellBounds(prevIndex, prevIndex);
                    }
                } else {
                    r = list.getCellBounds(index, index);
                }
                listRepaint(list, r);
                prevIndex = -1;
            } else {
                button.getModel().setRollover(true);
                renderer.rolloverIndex = index;
                if (!button.equals(prevButton)) {
                    Rectangle r = list.getCellBounds(prevIndex, index);
                    listRepaint(list, r);
                }
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
            if (button != null) {
                ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
                renderer.button = button;
                Rectangle r = list.getCellBounds(index, index);
                listRepaint(list, r);
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        JList list = (JList) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (button != null) {
                ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
                renderer.button = null;
                button.doClick();
                Rectangle r = list.getCellBounds(index, index);
                listRepaint(list, r);
            }
        }
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

class ButtonsRenderer extends JPanel implements ListCellRenderer<String> {
    private static final Color EVEN_COLOR = new Color(230, 255, 230);
    private final JComboBox comboBox;
    private JList list;
    private int index;
    public int rolloverIndex = -1;
    public JButton button;
    private final JLabel label = new DefaultListCellRenderer();
    private final JButton deleteButton = new JButton(new AbstractAction("x") {
        @Override public void actionPerformed(ActionEvent e) {
            MutableComboBoxModel m = (MutableComboBoxModel) list.getModel();
            if (m.getSize() > 1) {
                m.removeElementAt(index);
                comboBox.showPopup();
            }
        }
    }) {
        @Override public Dimension getPreferredSize() {
            return new Dimension(16, 16);
        }
    };
    public ButtonsRenderer(JComboBox comboBox) {
        super(new BorderLayout(0, 0));
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
//  @Override public Component getListCellRendererComponent(JList list, E value, int index, boolean isSelected, boolean hasFocus) {
    @Override public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
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
            setOpaque(true);
            deleteButton.setVisible(true);
            deleteButton.setForeground(list.getForeground());
            if (button != null && index == rolloverIndex) {
                button.setForeground(Color.WHITE);
            }
        }
        return this;
    }
}
