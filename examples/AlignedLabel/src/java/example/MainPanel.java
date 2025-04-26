// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    AlignedLabel fileName = new AlignedLabel("File Name:");
    AlignedLabel filesOfType = new AlignedLabel("Files of Type:");
    AlignedLabel host = new AlignedLabel("Host:");
    AlignedLabel port = new AlignedLabel("Port:");
    AlignedLabel user = new AlignedLabel("User Name:");
    AlignedLabel password = new AlignedLabel("Password:");
    AlignedLabel.groupLabels(fileName, filesOfType, host, port, user, password);

    Border innerBorder = BorderFactory.createEmptyBorder(5, 2, 5, 5);

    Box box1 = Box.createVerticalBox();
    TitledBorder border1 = BorderFactory.createTitledBorder("FileChooser");
    border1.setTitlePosition(TitledBorder.ABOVE_TOP);
    box1.setBorder(BorderFactory.createCompoundBorder(border1, innerBorder));
    box1.add(makeLabeledBox(fileName, new JTextField()));
    box1.add(Box.createVerticalStrut(5));
    box1.add(makeLabeledBox(filesOfType, new JComboBox<String>()));

    Box box2 = Box.createVerticalBox();
    TitledBorder border2 = BorderFactory.createTitledBorder("HTTP Proxy");
    border2.setTitlePosition(TitledBorder.ABOVE_TOP);
    box2.setBorder(BorderFactory.createCompoundBorder(border2, innerBorder));
    box2.add(makeLabeledBox(host, new JTextField()));
    box2.add(Box.createVerticalStrut(5));
    box2.add(makeLabeledBox(port, new JTextField()));
    box2.add(Box.createVerticalStrut(5));
    box2.add(makeLabeledBox(user, new JTextField()));
    box2.add(Box.createVerticalStrut(5));
    box2.add(makeLabeledBox(password, new JPasswordField()));

    Box box = Box.createVerticalBox();
    box.add(box1);
    box.add(Box.createVerticalStrut(10));
    box.add(box2);

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeLabeledBox(Component label, Component c) {
    Box box = Box.createHorizontalBox();
    box.add(label);
    box.add(Box.createHorizontalStrut(5));
    box.add(c);
    return box;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see javax/swing/plaf/metal/MetalFileChooserUI.java
class AlignedLabel extends JLabel {
  private static final int INDENT = 10;
  // private AlignedLabel[] group;
  private List<AlignedLabel> group;
  private int maxWidth;

  protected AlignedLabel(String text) {
    super(text, RIGHT);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    // Align the width with all other labels in group.
    d.width = getMaxWidth() + INDENT;
    return d;
  }

  private int getMaxWidth() {
    if (maxWidth == 0 && Objects.nonNull(group)) {
      // int max = 0;
      // for (AlignedLabel al : group) {
      //  max = Math.max(al.getSuperPreferredWidth(), max);
      // }
      // for (AlignedLabel al : group) {
      //   al.maxWidth = max;
      // }
      int max = group.stream()
          .map(AlignedLabel::getSuperPreferredWidth)
          .reduce(0, Integer::max);
      group.forEach(al -> al.maxWidth = max);
    }
    return maxWidth;
  }

  private int getSuperPreferredWidth() {
    return super.getPreferredSize().width;
  }

  public static void groupLabels(AlignedLabel... list) {
    List<AlignedLabel> group = Arrays.asList(list);
    group.forEach(al -> al.group = group);
  }
}
