package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextArea log = new JTextArea();

        JButton button1 = new JButton("Default");
        button1.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int retvalue = chooser.showOpenDialog(getRootPane());
            if (retvalue == JFileChooser.APPROVE_OPTION) {
                log.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton button2 = new JButton("Details View");
        button2.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            // java - How can I start the JFileChooser in the Details view? - Stack Overflow
            // https://stackoverflow.com/questions/16292502/how-can-i-start-the-jfilechooser-in-the-details-view
            Action detailsAction = chooser.getActionMap().get("viewTypeDetails");
            if (Objects.nonNull(detailsAction)) {
                detailsAction.actionPerformed(null);
            }

            // TEST1: searchAndResizeMode(chooser);
            // TEST2: Component c = findChildComponent(chooser, JTable.class); if (c instanceof JTable) { ... }
            // TEST3: getComponentByClass(chooser, JTable.class).ifPresent(t -> t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN));

            // TEST4:
            stream(chooser)
                .filter(JTable.class::isInstance).map(JTable.class::cast)
                .findFirst()
                .ifPresent(t -> t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN));

            int retvalue = chooser.showOpenDialog(getRootPane());
            if (retvalue == JFileChooser.APPROVE_OPTION) {
                log.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
        p.add(button1);
        p.add(button2);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
        setPreferredSize(new Dimension(320, 240));
    }

    // TEST1
    public static boolean searchAndResizeMode(Container parent) {
        for (Component c: parent.getComponents()) {
            if (c instanceof JTable) {
                ((JTable) c).setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                return true;
            } else if (c instanceof Container && searchAndResizeMode((Container) c)) {
                return true;
            }
        }
        return false;
    }

    // TEST2
    public static Component findChildComponent(Container container, Class<? extends Component> cls) {
        int n = container.getComponentCount();
        for (int i = 0; i < n; i++) {
            Component comp = container.getComponent(i);
            if (cls.isInstance(comp)) {
                return comp;
            } else if (comp instanceof Container) {
                Component c = findChildComponent((Container) comp, cls);
                if (Objects.nonNull(c)) {
                    return c;
                }
            }
        }
        return null;
    }

    // TEST3
    public static <T> Optional<T> getComponentByClass(Container parent, Class<T> clz) {
        if (clz.isInstance(parent)) {
            return Optional.of(clz.cast(parent));
        }
        for (Component c: parent.getComponents()) {
            if (c instanceof Container) {
                Optional<T> op = getComponentByClass((Container) c, clz);
                if (op.isPresent()) {
                    return op;
                }
            }
        }
        return Optional.empty();
    }

    // TEST4
    public static Stream<Component> stream(Container parent) {
        return Arrays.stream(parent.getComponents())
            .filter(Container.class::isInstance).map(c -> stream(Container.class.cast(c)))
            .reduce(Stream.of(parent), Stream::concat);
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
