package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JLabel label = new JLabel();

    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        label.addHierarchyListener(new AutomaticallyCloseListener(textArea));
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(makePanel("HierarchyListener", frame, label));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private JPanel makePanel(String title, final JFrame frame, final JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JButton(new AbstractAction("show") {
            @Override public void actionPerformed(ActionEvent e) {
                int r = JOptionPane.showConfirmDialog(frame, c, "Automatically close dialog", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                switch(r) {
                  case JOptionPane.OK_OPTION:
                    textArea.append("OK\n"); break;
                  case JOptionPane.CANCEL_OPTION:
                    textArea.append("Cancel\n"); break;
                  case JOptionPane.CLOSED_OPTION:
                    textArea.append("Closed(automatically)\n"); break;
                  default:
                    textArea.append("----\n");
                }
                textArea.append("\n");
            }
        }));
        return p;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class AutomaticallyCloseListener implements HierarchyListener {
    private static final int SECONDS = 5;
    private final AtomicInteger atomicDown = new AtomicInteger(SECONDS);
    private Timer timer;
    private final JTextArea textArea;
    public AutomaticallyCloseListener(JTextArea textArea) {
        this.textArea = textArea;
    }
    private Timer makeTimer(final JLabel l) {
        return new Timer(1000, new ActionListener() {
            //private int countdown = SECONDS;
            @Override public void actionPerformed(ActionEvent e) {
                //int i = --countdown;
                int i = atomicDown.decrementAndGet();
                l.setText(String.format("Closing in %d seconds", i));
                if(i<=0) {
                    Window w = SwingUtilities.getWindowAncestor(l);
                    if(w!=null && timer!=null && timer.isRunning()) {
                        textArea.append("Timer: timer.stop()\n");
                        timer.stop();
                        textArea.append("window.dispose()\n");
                        w.dispose();
                    }
                }
            }
        });
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)!=0) {
            JLabel l = (JLabel)e.getComponent();
            if(l.isShowing()) {
                textArea.append("isShowing=ture\n");
                atomicDown.set(SECONDS);
                l.setText(String.format("Closing in %d seconds", SECONDS));
                timer = makeTimer(l);
                timer.start();
            }else{
                textArea.append("isShowing=false\n");
                if(timer!=null && timer.isRunning()) {
                    textArea.append("timer.stop()\n");
                    timer.stop();
                    timer = null;
                }
            }
        }
    }
}
