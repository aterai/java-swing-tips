package example;

import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("Spinner.editorAlignment", SwingConstants.CENTER);
    // System.out.println(UIManager.get("Spinner.editorAlignment"));
    JRadioButton r1 = new JRadioButton("LEADING");
    JRadioButton r2 = new JRadioButton("CENTER");
    JRadioButton r3 = new JRadioButton("TRAILING");
    ItemListener il = e -> {
      int alignment;
      if (e.getItemSelectable() == r1) {
        alignment = SwingConstants.LEADING;
      } else if (e.getItemSelectable() == r2) {
        alignment = SwingConstants.CENTER;
      } else {
        alignment = SwingConstants.TRAILING;
      }
      UIManager.put("Spinner.editorAlignment", alignment);
      SwingUtilities.updateComponentTreeUI(this);
    };
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createHorizontalBox();
    for (JRadioButton r : Arrays.asList(r1, r2, r3)) {
      r.addItemListener(il);
      bg.add(r);
      box.add(r);
    }
    JTextArea log = new JTextArea();
    log.setEditable(false);

    List<String> weeks = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Sat");
    JSpinner spinner0 = new JSpinner(new SpinnerListModel(weeks));
    String str0 = getHorizontalAlignment((JSpinner.DefaultEditor) spinner0.getEditor());
    log.append("SpinnerListModel: " + str0 + "\n");

    @SuppressWarnings("JavaUtilDate")
    Date d = new Date();
    JSpinner spinner1 = new JSpinner(new SpinnerDateModel(d, d, null, Calendar.DAY_OF_MONTH));
    spinner1.setEditor(new JSpinner.DateEditor(spinner1, "yyyy/MM/dd"));
    String str1 = getHorizontalAlignment((JSpinner.DefaultEditor) spinner1.getEditor());
    log.append("SpinnerDateModel: " + str1 + "\n");

    JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(5, 0, 10, 1));
    String str2 = getHorizontalAlignment((JSpinner.DefaultEditor) spinner2.getEditor());
    log.append("SpinnerNumberModel: " + str2 + "\n");

    JPanel p = new JPanel(new BorderLayout());
    p.add(box, BorderLayout.NORTH);
    p.add(makeSpinnerPanel(spinner0, spinner1, spinner2));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeSpinnerPanel(JSpinner sp0, JSpinner sp1, JSpinner sp2) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.weightx = 0.0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.EAST;

    JPanel p = new JPanel(new GridBagLayout());

    c.gridy = 0;
    p.add(new JLabel("SpinnerListModel: "), c);
    c.gridy = 1;
    p.add(new JLabel("SpinnerDateModel: "), c);
    c.gridy = 2;
    p.add(new JLabel("SpinnerNumberModel: "), c);

    c.gridx = 1;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridy = 0;
    p.add(sp0, c);
    c.gridy = 1;
    p.add(sp1, c);
    c.gridy = 2;
    p.add(sp2, c);
    return p;
  }

  private static String getHorizontalAlignment(JSpinner.DefaultEditor editor) {
    switch (editor.getTextField().getHorizontalAlignment()) {
      case SwingConstants.LEFT:
        return "LEFT";
      case SwingConstants.CENTER:
        return "CENTER";
      case SwingConstants.RIGHT:
        return "RIGHT";
      case SwingConstants.LEADING:
        return "LEADING";
      case SwingConstants.TRAILING:
        return "TRAILING";
      default:
        return "ERROR";
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
