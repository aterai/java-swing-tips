package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("11\n1");
        model.addElement("222222222222222\n222222222222222");
        model.addElement("3333333333333333333\n33333333333333333333\n33333333333333333");
        model.addElement("444");

        add(new JScrollPane(new JList<String>(model) {
            private transient MouseInputListener cbml;
            @Override public void updateUI() {
                removeMouseListener(cbml);
                removeMouseMotionListener(cbml);
                super.updateUI();
                setFixedCellHeight(-1);
                cbml = new CellButtonsMouseListener<>(this);
                addMouseListener(cbml);
                addMouseMotionListener(cbml);
                setCellRenderer(new ButtonsRenderer<>(model));
            }
        }));
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

class CellButtonsMouseListener<E> extends MouseInputAdapter {
    private int prevIndex = -1;
    private JButton prevButton;
    private final JList<E> list;
    protected CellButtonsMouseListener(JList<E> list) {
        super();
        this.list = list;
    }
    @Override public void mouseMoved(MouseEvent e) {
        // JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (!list.getCellBounds(index, index).contains(pt)) {
            if (prevIndex >= 0) {
                rectRepaint(list, list.getCellBounds(prevIndex, prevIndex));
            }
            index = -1;
            prevButton = null;
            return;
        }
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            ButtonsRenderer<? super E> renderer = (ButtonsRenderer<? super E>) list.getCellRenderer();
            renderer.button = button;
            if (Objects.nonNull(button)) {
                button.getModel().setRollover(true);
                renderer.rolloverIndex = index;
                if (!button.equals(prevButton)) {
                    rectRepaint(list, list.getCellBounds(prevIndex, index));
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
        // JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (Objects.nonNull(button)) {
                ButtonsRenderer<? super E> renderer = (ButtonsRenderer<? super E>) list.getCellRenderer();
                renderer.pressedIndex = index;
                renderer.button = button;
                rectRepaint(list, list.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        // JList<?> list = (JList<?>) e.getComponent();
        Point pt = e.getPoint();
        int index = list.locationToIndex(pt);
        if (index >= 0) {
            JButton button = getButton(list, pt, index);
            if (Objects.nonNull(button)) {
                ButtonsRenderer<? super E> renderer = (ButtonsRenderer<? super E>) list.getCellRenderer();
                renderer.pressedIndex = -1;
                renderer.button = null;
                button.doClick();
                rectRepaint(list, list.getCellBounds(index, index));
            }
        }
    }
    private static void rectRepaint(JComponent c, Rectangle rect) {
        Optional.ofNullable(rect).ifPresent(c::repaint);
    }
    private static <E> JButton getButton(JList<E> list, Point pt, int index) {
        Component c = list.getCellRenderer().getListCellRendererComponent(list, list.getPrototypeCellValue(), index, false, false);
        Rectangle r = list.getCellBounds(index, index);
        c.setBounds(r);
        // c.doLayout(); // may be needed for mone LayoutManager
        pt.translate(-r.x, -r.y);
//         Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
//         if (b instanceof JButton) {
//             return (JButton) b;
//         } else {
//             return null;
//         }
        return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
            .filter(JButton.class::isInstance).map(JButton.class::cast).orElse(null);
    }
}

class ButtonsRenderer<E> extends JPanel implements ListCellRenderer<E> {
    protected static final Color EVEN_COLOR = new Color(230, 255, 230);
    protected final JTextArea textArea = new JTextArea();
    protected final JButton deleteButton = new JButton("delete");
    protected final JButton copyButton = new JButton("copy");
    protected final List<JButton> buttons = Arrays.asList(deleteButton, copyButton);
    protected final DefaultListModel<E> model;
    protected int targetIndex;
    protected int pressedIndex = -1;
    protected int rolloverIndex = -1;
    protected JButton button;

    protected ButtonsRenderer(DefaultListModel<E> model) {
        super(new BorderLayout());
        this.model = model;
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        setOpaque(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        add(textArea);

        deleteButton.addActionListener(e -> {
            boolean isMoreThanOneItem = model.getSize() > 1;
            if (isMoreThanOneItem) {
                model.remove(targetIndex);
            }
        });
        copyButton.addActionListener(e -> model.add(targetIndex, model.get(targetIndex)));

        Box box = Box.createHorizontalBox();
        buttons.forEach(b -> {
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            box.add(b);
            box.add(Box.createHorizontalStrut(5));
        });
        add(box, BorderLayout.EAST);
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 0; // VerticalScrollBar as needed
        return d;
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        textArea.setText(Objects.toString(value, ""));
        this.targetIndex = index;
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            textArea.setForeground(list.getSelectionForeground());
        } else {
            setBackground(index % 2 == 0 ? EVEN_COLOR : list.getBackground());
            textArea.setForeground(list.getForeground());
        }
        buttons.forEach(ButtonsRenderer::resetButtonStatus);
        if (Objects.nonNull(button)) {
            if (index == pressedIndex) {
                button.getModel().setSelected(true);
                button.getModel().setArmed(true);
                button.getModel().setPressed(true);
            } else if (index == rolloverIndex) {
                button.getModel().setRollover(true);
            }
        }
        return this;
    }
    private static void resetButtonStatus(AbstractButton button) {
        ButtonModel model = button.getModel();
        model.setRollover(false);
        model.setArmed(false);
        model.setPressed(false);
        model.setSelected(false);
    }
}
