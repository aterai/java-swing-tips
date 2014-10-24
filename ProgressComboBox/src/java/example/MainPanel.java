package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<String> combo = new JComboBox<>();
    private final JButton button;
    private SwingWorker<String[], Integer> worker;
    private int counter;
    public MainPanel() {
        super(new BorderLayout());
        combo.setRenderer(new ProgressCellRenderer());
        button = new JButton(new AbstractAction("load") {
            @Override public void actionPerformed(ActionEvent e) {
                button.setEnabled(false);
                combo.setEnabled(false);
                //combo.removeAllItems();
                worker = new Task() {
                    @Override protected void process(List<Integer> chunks) {
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
                };
                worker.execute();
            }
        });
        add(createPanel(combo, button, "ProgressComboBox: "), BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320, 240));
    }

    class ProgressCellRenderer extends DefaultListCellRenderer {
        private final JProgressBar bar = new JProgressBar() {
            @Override public Dimension getPreferredSize() {
                return ProgressCellRenderer.this.getPreferredSize();
            }
        };
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (index < 0 && worker != null && !worker.isDone()) {
                bar.setFont(list.getFont());
                bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                bar.setValue(counter);
                return bar;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        @Override public void updateUI() {
            super.updateUI();
            if (bar != null) {
                SwingUtilities.updateComponentTreeUI(bar);
            }
        }
    }

    public JPanel createPanel(JComponent cmp, JButton btn, String str) {
//         JPanel panel = new JPanel(new BorderLayout(5, 5));
//         panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//         panel.add(new JLabel(str), BorderLayout.WEST);
//         panel.add(cmp);
//         panel.add(btn, BorderLayout.EAST);

        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());

        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(str), c);

        c.gridx = 1;
        c.weightx = 1.0;
        //c.insets = new Insets(5, 5, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmp, c);

        c.gridx = 2;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        panel.add(btn, c);

        return panel;
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
