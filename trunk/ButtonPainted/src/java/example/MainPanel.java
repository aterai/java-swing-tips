package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

class MainPanel extends JPanel {
    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        JMenuBar mb = new JMenuBar();
        mb.add(createLookAndFeelMenu());
        frame.setJMenuBar(mb);

        ImageIcon rss = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); //http://feedicons.com/
        JButton b1 = new JButton("button");
        JButton b2 = new JButton(rss);
        b2.setRolloverIcon(makeRolloverIcon(rss));

        final List<JButton> list = Arrays.asList(b1, b2);
        final JPanel p = new JPanel();
        p.add(b1);
        p.add(b2);

        List<JCheckBox> clist = Arrays.asList(
            new JCheckBox(new AbstractAction("setFocusPainted") {
                @Override public void actionPerformed(ActionEvent e) {
                    boolean flg = ((JCheckBox)e.getSource()).isSelected();
                    for(JButton b:list) b.setFocusPainted(flg);
                    p.revalidate();
                }
            }),
            new JCheckBox(new AbstractAction("setBorderPainted") {
                @Override public void actionPerformed(ActionEvent e) {
                    boolean flg = ((JCheckBox)e.getSource()).isSelected();
                    for(JButton b:list) b.setBorderPainted(flg);
                    p.revalidate();
                }
            }),
            new JCheckBox(new AbstractAction("setContentAreaFilled") {
                @Override public void actionPerformed(ActionEvent e) {
                    boolean flg = ((JCheckBox)e.getSource()).isSelected();
                    for(JButton b:list) b.setContentAreaFilled(flg);
                    p.revalidate();
                }
            }),
            new JCheckBox(new AbstractAction("setRolloverEnabled") {
                @Override public void actionPerformed(ActionEvent e) {
                    boolean flg = ((JCheckBox)e.getSource()).isSelected();
                    for(JButton b:list) b.setRolloverEnabled(flg);
                    p.revalidate();
                }
            })
//             new JCheckBox(new AbstractAction("setBorder(null:BorderFactory.createLineBorder)") {
//                 public void actionPerformed(ActionEvent e) {
//                     boolean flg = ((JCheckBox)e.getSource()).isSelected();
//                     Border border = (flg)?BorderFactory.createLineBorder(Color.RED, 5):null;
//                     for(JButton b:list) b.setBorder(border);
//                     p.revalidate();
//                 }
//             }),
        );
        Box box = Box.createVerticalBox();
        for(JCheckBox c:clist) {
            c.setSelected(true);
            box.add(c);
        }
        add(box, BorderLayout.NORTH);
        add(p);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
    }
    private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] { 1.2f,1.2f,1.2f,1.0f },
            new float[] { 0f,0f,0f,0f }, null);
        BufferedImage img = new BufferedImage(
            srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        //g.drawImage(srcIcon.getImage(), 0, 0, null);
        srcIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(op.filter(img, null));
    }
    private JComponent makeTitlePanel(String title, List<? extends JComponent> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.gridy   = 0;
        for(JComponent cmp:list) {
            p.add(cmp, c);
            c.gridy++;
        }
        return p;
    }
    //<blockquote cite="http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java">
    private ButtonGroup lookAndFeelRadioGroup;
    private String lookAndFeel;
    protected JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
        }
        return menu;
    }
    protected JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try{
                    setLookAndFeel(m.getActionCommand());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    public void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = this.lookAndFeel;
        if(!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            this.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private void updateLookAndFeel() {
        for(Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
