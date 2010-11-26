package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JCheckBox check = new JCheckBox("JCheckBox#setIcon(...)");
        check.setIcon(new CheckIcon());
        add(check, BorderLayout.SOUTH);
        add(new JTextArea());
        setPreferredSize(new Dimension(320, 180));
    }
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("JMenu");
        menuBar.add(menu);
        menu.add(new JCheckBoxMenuItem("default"));
        UIManager.put("CheckBoxMenuItem.checkIcon", new CheckIcon());
        menu.add(new JCheckBoxMenuItem("checkIcon test"));

        JMenu menu2 = new JMenu("JMenu2");
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("setIcon");
        jcbmi.setIcon(new CheckIcon());
        //jcbmi.setSelectedIcon(new CheckIcon());
        menu2.add(jcbmi);
        menuBar.add(menu);
        menuBar.add(menu2);
        return menuBar;
    }
    private void buttonActionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, "Error Message", "Title", JOptionPane.ERROR_MESSAGE);
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
        MainPanel p;
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p = new MainPanel());
        frame.setJMenuBar(p.createMenuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class CheckIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(x,y);
        ButtonModel m = ((AbstractButton)c).getModel();
        g2.setPaint(m.isSelected()?Color.ORANGE:Color.GRAY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillOval( 0, 2, 10, 10 );
        g2.translate(-x,-y);
    }
    @Override public int getIconWidth()  { return 14; }
    @Override public int getIconHeight() { return 14; }
}
