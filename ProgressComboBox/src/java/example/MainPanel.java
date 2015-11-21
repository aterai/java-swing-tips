package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<String> combo = new JComboBox<>();
    private final JButton button;
    private SwingWorker<String[], Integer> worker;
    private int counter;
    public MainPanel() {
        super(new BorderLayout(5, 5));
        combo.setRenderer(new ProgressCellRenderer());
        button = new JButton(new AbstractAction("load") {
            @Override public void actionPerformed(ActionEvent e) {
                button.setEnabled(false);
                combo.setEnabled(false);
                //combo.removeAllItems();
                worker = new UITask();
                worker.execute();
            }
        });
        add(createPanel(combo, button, "ProgressComboBox: "), BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }

    class UITask extends Task {
        @Override protected void process(List<Integer> chunks) {
            if (isCancelled()) {
                return;
            }
            if (!isDisplayable()) {
                System.out.println("process: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            for (Integer i: chunks) {
                counter = i;
            }
            combo.setSelectedIndex(-1);
            combo.repaint();
        }
        @Override public void done() {
            if (!isDisplayable()) {
                System.out.println("done: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            try {
                if (!isCancelled()) {
                    String[] array = get();
                    combo.setModel(new DefaultComboBoxModel<String>(array));
                    combo.setSelectedIndex(0);
                }
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("Interrupted");
            }
            combo.setEnabled(true);
            button.setEnabled(true);
            counter = 0;
        }
    }

    class ProgressCellRenderer extends DefaultListCellRenderer {
        private final JProgressBar bar = new JProgressBar() {
            @Override public Dimension getPreferredSize() {
                return ProgressCellRenderer.this.getPreferredSize();
            }
        };
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (index < 0 && Objects.nonNull(worker) && !worker.isDone()) {
                bar.setFont(list.getFont());
                bar.setBorder(BorderFactory.createEmptyBorder());
                bar.setValue(counter);
                return bar;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        @Override public void updateUI() {
            super.updateUI();
            if (Objects.nonNull(bar)) {
                SwingUtilities.updateComponentTreeUI(bar);
            }
        }
    }

    public JPanel createPanel(JComponent cmp, JButton btn, String str) {
        GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());

        c.insets = new Insets(5, 5, 5, 0);
        p.add(new JLabel(str), c);

        c.weightx = 1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(cmp, c);

        c.weightx = 0d;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 5, 5, 5);
        p.add(btn, c);

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
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String[], Integer> {
    private static final int MAX = 30;
    @Override public String[] doInBackground() {
        int current = 0;
        List<String> list = new ArrayList<>();
        while (current <= MAX && !isCancelled()) {
            try {
                Thread.sleep(50);
                int iv = 100 * current / MAX;
                publish(iv);
                //setProgress(iv);
                list.add("Test: " + current);
            } catch (InterruptedException ie) {
                break;
            }
            current++;
        }
        return list.toArray(new String[list.size()]);
    }
}
