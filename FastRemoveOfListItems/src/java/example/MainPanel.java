package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("default remove", makeUI0());
        tabbedPane.addTab("clear + addElement", makeUI1());
        tabbedPane.addTab("addAll + remove", makeUI2());
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    private static <E> void move0(JList<E> from, JList<E> to) {
        int[] selectedIndices = from.getSelectedIndices();
        if (selectedIndices.length > 0) {
            DefaultListModel<E> fromModel = (DefaultListModel<E>) from.getModel();
            DefaultListModel<E> toModel = (DefaultListModel<E>) to.getModel();
            for (int i: selectedIndices) {
                toModel.addElement(fromModel.get(i));
            }
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                fromModel.remove(selectedIndices[i]);
            }
        }
    }
    private static <E> void move1(JList<E> from, JList<E> to) {
        ListSelectionModel sm = from.getSelectionModel();
        int[] selectedIndices = from.getSelectedIndices();

        DefaultListModel<E> fromModel = (DefaultListModel<E>) from.getModel();
        DefaultListModel<E> toModel = (DefaultListModel<E>) to.getModel();
        List<E> unselectedValues = new ArrayList<>();
        for (int i = 0; i < fromModel.getSize(); i++) {
            if (!sm.isSelectedIndex(i)) {
                unselectedValues.add(fromModel.getElementAt(i));
            }
        }
        if (selectedIndices.length > 0) {
            for (int i: selectedIndices) {
                toModel.addElement(fromModel.get(i));
            }
            // if (from.getSelectionMode() != ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
            //     fromModel.removeRange(selectedIndices[0], selectedIndices[selectedIndices.length - 1]);
            // }
            // TEST: Moving the first item is very slow.
            // fromModel.clear();
            // unselectedValues.forEach(fromModel::addElement);
            DefaultListModel<E> model = new DefaultListModel<>();
            unselectedValues.forEach(model::addElement);
            from.setModel(model);
        }
    }
    private static <E> void move2(JList<E> from, JList<E> to) {
        int[] selectedIndices = from.getSelectedIndices();
        if (selectedIndices.length > 0) {
            ((ArrayListModel<E>) to.getModel()).addAll(from.getSelectedValuesList());
            ((ArrayListModel<E>) from.getModel()).remove(selectedIndices);
        }
    }
    private static <E> JList<E> makeList(ListModel<E> model) {
        JList<E> list = new JList<>(model);
        JPopupMenu popup = new JPopupMenu();
        popup.add("reverse").addActionListener(e -> {
            ListSelectionModel sm = list.getSelectionModel();
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (sm.isSelectedIndex(i)) {
                    sm.removeSelectionInterval(i, i);
                } else {
                    sm.addSelectionInterval(i, i);
                }
            }
        });
        list.setComponentPopupMenu(popup);
        return list;
    }
    private static JButton makeButton(String title) {
        JButton button = new JButton(title);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return button;
    }
    private Component makeUI0() {
        DefaultListModel<String> model = new DefaultListModel<>();
        IntStream.range(0, 5000).mapToObj(Objects::toString).forEach(model::addElement);
        JList<String> leftList = makeList(model);

        JList<String> rightList = makeList(new DefaultListModel<>());

        JButton button1 = makeButton(">");
        button1.addActionListener(e -> move0(leftList, rightList));

        JButton button2 = makeButton("<");
        button2.addActionListener(e -> move0(rightList, leftList));

        return SpringLayoutUtil.makePanel(leftList, rightList, button1, button2);
    }
    private Component makeUI1() {
        DefaultListModel<String> model = new DefaultListModel<>();
        IntStream.range(10000, 30000).mapToObj(Objects::toString).forEach(model::addElement);
        JList<String> leftList = makeList(model);

        JList<String> rightList = makeList(new DefaultListModel<>());

        JButton button1 = makeButton(">");
        button1.addActionListener(e -> move1(leftList, rightList));

        JButton button2 = makeButton("<");
        button2.addActionListener(e -> move1(rightList, leftList));

        return SpringLayoutUtil.makePanel(leftList, rightList, button1, button2);
    }
    private Component makeUI2() {
        ArrayListModel<String> model = new ArrayListModel<>();
        IntStream.range(30000, 50000).mapToObj(Objects::toString).forEach(model::add);
        JList<String> leftList = makeList(model);

        JList<String> rightList = makeList(new ArrayListModel<>());

        JButton button1 = makeButton(">");
        button1.addActionListener(e -> move2(leftList, rightList));

        JButton button2 = makeButton("<");
        button2.addActionListener(e -> move2(rightList, leftList));

        return SpringLayoutUtil.makePanel(leftList, rightList, button1, button2);
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

class ArrayListModel<E> extends AbstractListModel<E> {
    private final List<E> delegate = new ArrayList<>();

    public void add(E element) {
        int index = delegate.size();
        delegate.add(element);
        fireIntervalAdded(this, index, index);
    }

    public void addAll(Collection<? extends E> c) {
        delegate.addAll(c);
        fireIntervalAdded(this, 0, delegate.size());
    }

    public E remove(int index) {
        E rv = delegate.get(index);
        delegate.remove(index);
        fireIntervalRemoved(this, index, index);
        return rv;
    }

    // public boolean removeAll(Collection<?> c) {
    //     int max = delegate.size();
    //     boolean b = delegate.removeAll(c);
    //     fireIntervalRemoved(this, 0, max);
    //     return b;
    // }

    public void remove(int... selectedIndices) {
        if (selectedIndices.length > 0) {
            int max = selectedIndices.length - 1;
            for (int i = max; i >= 0; i--) {
                delegate.remove(selectedIndices[i]);
            }
            fireIntervalRemoved(this, selectedIndices[0], selectedIndices[max]);
        }
    }

    @Override public E getElementAt(int index) {
        return delegate.get(index);
    }

    @Override public int getSize() {
        return delegate.size();
    }
}

final class SpringLayoutUtil {
    private SpringLayoutUtil() { /* Singleton */ }
    public static void setScaleAndAdd(Container parent, SpringLayout layout, Component child, float sx, float sy, float sw, float sh) {
        Spring panelw = layout.getConstraint(SpringLayout.WIDTH, parent);
        Spring panelh = layout.getConstraint(SpringLayout.HEIGHT, parent);

        SpringLayout.Constraints c = layout.getConstraints(child);
        c.setX(Spring.scale(panelw, sx));
        c.setY(Spring.scale(panelh, sy));
        c.setWidth(Spring.scale(panelw, sw));
        c.setHeight(Spring.scale(panelh, sh));

        parent.add(child);
    }
    public static Component makePanel(JList<?> leftList, JList<?> rightList, JButton l2rButton, JButton r2lButton) {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        box.add(Box.createVerticalGlue());
        box.add(l2rButton);
        box.add(Box.createVerticalStrut(20));
        box.add(r2lButton);
        box.add(Box.createVerticalGlue());

        JPanel cpn = new JPanel(new GridBagLayout());
        cpn.add(box);

        JScrollPane spl = new JScrollPane(leftList);
        JScrollPane spr = new JScrollPane(rightList);

        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout);
        setScaleAndAdd(p, layout, spl, .05f, .05f, .40f, .90f);
        setScaleAndAdd(p, layout, cpn, .45f, .05f, .10f, .90f);
        setScaleAndAdd(p, layout, spr, .55f, .05f, .40f, .90f);
        return p;
    }
}
