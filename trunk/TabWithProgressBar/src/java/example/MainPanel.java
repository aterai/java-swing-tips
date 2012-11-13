package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.concurrent.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final Executor executor = Executors.newCachedThreadPool();
    private final JTabbedPane tab = new JTabbedPane() {
        @Override public void addTab(String title, final Component content) {
            super.addTab(title, new JLabel("Loading..."));
            final JProgressBar bar = new JProgressBar();
            final int currentIndex = getTabCount()-1;
            final JLabel label = new JLabel(title);
            Dimension dim = label.getPreferredSize();
            int w = Math.max(80, dim.width);
            label.setPreferredSize(new Dimension(w, dim.height));
            Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
            bar.setPreferredSize(new Dimension(w, dim.height-tabInsets.top-1));
            //bar.setString(title);
            //bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI());
            setTabComponentAt(currentIndex, bar);
            SwingWorker worker = new SwingWorker() {
                @Override public Object doInBackground() {
                    int current = 0;
                    int lengthOfTask = 120;
                    while(current<lengthOfTask) {
                        if(!bar.isDisplayable()) {
                            return "NotDisplayable or Disposed";
                        }
                        try{
                            Thread.sleep(20);
                        }catch(InterruptedException ie) {
                            ie.printStackTrace();
                            return "Interrupted";
                        }
                        current++;
                        setProgress(100 * current / lengthOfTask);
                    }
                    return "Done";
                }
                @Override public void done() {
                    setTabComponentAt(currentIndex, label);
                    setComponentAt(currentIndex, content);
                    Object o = null;
                    try{
                        o = get();
                    }catch(Exception e) {
                        o = "Exception";
                    }
                    System.out.println(o);
                }
            };
            worker.addPropertyChangeListener(new ProgressListener(bar));
            executor.execute(worker);
            //worker.execute();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        tab.setComponentPopupMenu(new TabPopupMenu());
        tab.addTab("PopupMenu+addTab", new JScrollPane(new JTree()));
        add(tab);
        add(new JButton(new NewTabAction("Add", null)), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private class TabPopupMenu extends JPopupMenu {
        public TabPopupMenu() {
            super();
            add(new NewTabAction("Add", null));
            addSeparator();
            add(new CloseAllAction("Close All", null));
        }
    }
    static private int count = 0;
    class NewTabAction extends AbstractAction{
        public NewTabAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            JComponent c = (count%2==0)?new JTree():new JLabel("Tab"+count);
            tab.addTab("Title"+count, c);
            tab.setSelectedIndex(tab.getTabCount()-1);
            count++;
        }
    }
    class CloseAllAction extends AbstractAction{
        public CloseAllAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.removeAll();
        }
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class ProgressListener implements PropertyChangeListener {
    private final JProgressBar progressBar;
    ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer)evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
}
