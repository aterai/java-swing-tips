package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        Point pt = new Point();
        Class<?> clz = getClass();
        Toolkit tk = Toolkit.getDefaultToolkit();
        List<Cursor> list = Stream.of("00", "01", "02")
            .map(s -> tk.createCustomCursor(tk.createImage(clz.getResource(s + ".png")), pt, s))
            .collect(Collectors.toList());

        Timer animator = new Timer(100, null);
        JButton button = new JButton("Start");
        button.setCursor(list.get(0));
        button.addActionListener(e -> {
            JButton b = (JButton) e.getSource();
            if (animator.isRunning()) {
                b.setText("Start");
                animator.stop();
            } else {
                b.setText("Stop");
                animator.start();
            }
        });
        button.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
                animator.stop();
            }
        });
        animator.addActionListener(new CursorActionListener(button, list));

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        p.add(button);
        add(p);
        setBorder(BorderFactory.createTitledBorder("delay=100ms"));
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
        // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class CursorActionListener implements ActionListener {
    private int counter;
    private final Component comp;
    private final List<Cursor> frames;
    protected CursorActionListener(Component comp, List<Cursor> frames) {
        this.comp = comp;
        this.frames = frames;
    }
    @Override public void actionPerformed(ActionEvent e) {
        comp.setCursor(frames.get(counter));
        counter = (counter + 1) % frames.size();
    }
}
