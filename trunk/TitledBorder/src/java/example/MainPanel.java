package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel {
    private static enum VerticalOrientation {
        DEFAULT_POSITION ("Default Position"),
        ABOVE_TOP        ("Above Top"),
        TOP              ("Top"),
        BELOW_TOP        ("Below Top"),
        ABOVE_BOTTOM     ("Above Bottom"),
        BOTTOM           ("Bottom"),
        BELOW_BOTTOM     ("Below Bottom");
        private final String description;
        private VerticalOrientation(String description) {
            this.description = description;
        }
        @Override public String toString() {
            return description;
        }
    }
    private static enum Justification {
        DEFAULT_JUSTIFICATION ("Default Justification"),
        LEFT                  ("Left"),
        CENTER                ("Center"),
        RIGHT                 ("Right"),
        LEADING               ("Leading"),
        TRAILING              ("Trailing");
        private final String description;
        private Justification(String description) {
            this.description = description;
        }
        @Override public String toString() {
            return description;
        }
    }
    private final JComboBox verticalOrientationChoices = makeComboBox(VerticalOrientation.values());
    private final JComboBox justificationChoices       = makeComboBox(Justification.values());
    private final TitledBorder border = BorderFactory.createTitledBorder("Test Test");
    private final JPanel panel = new JPanel();

    public MainPanel() {
        super(new BorderLayout());
        verticalOrientationChoices.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    initTitleBorder();
                }
            }
        });
        justificationChoices.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    initTitleBorder();
                }
            }
        });
        panel.setBorder(border);

        JPanel p2 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;
        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p2.add(new JLabel(" TitlePosition"), c);
        c.gridy   = 1; p2.add(new JLabel(" TitleJustification"), c);
        c.gridx   = 1;
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p2.add(verticalOrientationChoices, c);
        c.gridy   = 1; p2.add(justificationChoices, c);

        add(p2, BorderLayout.NORTH); add(panel);
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        setPreferredSize(new Dimension(320, 180));
    }
    private void initTitleBorder() {
        VerticalOrientation vo = (VerticalOrientation)verticalOrientationChoices.getSelectedItem();
        switch(vo) {
          case DEFAULT_POSITION: border.setTitlePosition(TitledBorder.DEFAULT_POSITION); break;
          case ABOVE_TOP:        border.setTitlePosition(TitledBorder.ABOVE_TOP);        break;
          case TOP:              border.setTitlePosition(TitledBorder.TOP);              break;
          case BELOW_TOP:        border.setTitlePosition(TitledBorder.BELOW_TOP);        break;
          case ABOVE_BOTTOM:     border.setTitlePosition(TitledBorder.ABOVE_BOTTOM);     break;
          case BOTTOM:           border.setTitlePosition(TitledBorder.BOTTOM);           break;
          case BELOW_BOTTOM:     border.setTitlePosition(TitledBorder.BELOW_BOTTOM);     break;
        }
        Justification jc = (Justification)justificationChoices.getSelectedItem();
        switch(jc) {
          case DEFAULT_JUSTIFICATION: border.setTitleJustification(TitledBorder.DEFAULT_JUSTIFICATION); break;
          case LEFT:     border.setTitleJustification(TitledBorder.LEFT);     break;
          case CENTER:   border.setTitleJustification(TitledBorder.CENTER);   break;
          case RIGHT:    border.setTitleJustification(TitledBorder.RIGHT);    break;
          case LEADING:  border.setTitleJustification(TitledBorder.LEADING);  break;
          case TRAILING: border.setTitleJustification(TitledBorder.TRAILING); break;
        }
        panel.repaint();
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox(Object[] model) {
        return new JComboBox(model);
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
