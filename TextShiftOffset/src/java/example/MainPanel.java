package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout(5,5));
        //System.out.println(UIManager.getInt("Button.textShiftOffset"));
        UIManager.put("Button.textShiftOffset", 0);

        List<JRadioButton> rl = Arrays.asList(
          new JRadioButton(new TextShiftOffsetAction(0)),
          new JRadioButton(new TextShiftOffsetAction(1)),
          new JRadioButton(new TextShiftOffsetAction(-1)));
        ButtonGroup bg = new ButtonGroup();
        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder("UIManager.put(\"Button.textShiftOffset\", offset)"));
        box.add(new JLabel("offset = "));
        boolean flag = true;
        for(JRadioButton rb: rl) {
            if(flag) {rb.setSelected(true); flag = false;}
            bg.add(rb);
            box.add(rb);
            box.add(Box.createHorizontalStrut(3));
        }
        box.add(Box.createHorizontalGlue());

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.add(new JButton("JButton"));
        p.add(new JButton(new ImageIcon(getClass().getResource("16x16.png"))));
        p.add(new JButton("<html>JButton<br>html<br>tag<br>test"));
        p.add(new JToggleButton("JToggleButton"));

        add(box, BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 180));
    }
    class TextShiftOffsetAction extends AbstractAction {
        private int offset = 0;
        public TextShiftOffsetAction(int offset) {
            super(" "+offset+" ");
            this.offset = offset;
        }
        @Override public void actionPerformed(ActionEvent e) {
            UIManager.put("Button.textShiftOffset", offset);
            SwingUtilities.updateComponentTreeUI(MainPanel.this);
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
