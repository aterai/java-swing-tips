// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
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
    AlignedLabel userName = new AlignedLabel("User Name:");
    AlignedLabel password = new AlignedLabel("Password:");
    AlignedLabel.groupLabels(fileName, filesOfType, host, port, userName, password);

    Box fileChooserBox = createTitledBox("FileChooser");
    fileChooserBox.add(createLabeledBox(fileName, new JTextField()));
    fileChooserBox.add(Box.createVerticalStrut(5));
    fileChooserBox.add(createLabeledBox(filesOfType, new JComboBox<String>()));

    Box proxyBox = createTitledBox("HTTP Proxy");
    proxyBox.add(createLabeledBox(host, new JTextField()));
    proxyBox.add(Box.createVerticalStrut(5));
    proxyBox.add(createLabeledBox(port, new JTextField()));
    proxyBox.add(Box.createVerticalStrut(5));
    proxyBox.add(createLabeledBox(userName, new JTextField()));
    proxyBox.add(Box.createVerticalStrut(5));
    proxyBox.add(createLabeledBox(password, new JPasswordField()));

    Box box = Box.createVerticalBox();
    box.add(fileChooserBox);
    box.add(Box.createVerticalStrut(10));
    box.add(proxyBox);
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box createTitledBox(String title) {
    TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
    titledBorder.setTitlePosition(TitledBorder.ABOVE_TOP);
    Border innerBorder = BorderFactory.createEmptyBorder(5, 2, 5, 5);
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createCompoundBorder(titledBorder, innerBorder));
    return box;
  }

  private static Box createLabeledBox(Component label, Component field) {
    Box box = Box.createHorizontalBox();
    box.add(label);
    box.add(Box.createHorizontalStrut(5));
    box.add(field);
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
      Logger.getGlobal().severe(ex::getMessage);
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
  // An ungrouped label is aligned only with itself.
  private List<AlignedLabel> group = Collections.singletonList(this);

  protected AlignedLabel(String text) {
    super(text, RIGHT);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    // Align the width with all other labels in group.
    d.width = getMaxWidth() + INDENT;
    return d;
  }

  // BasicLabelUI calculates the minimum and maximum sizes without calling
  // the overridden getPreferredSize(), so BoxLayout would resize the labels unevenly.
  @Override public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  // The width is not cached so that changes of the text or font are reflected.
  private int getMaxWidth() {
    return group.stream()
        .mapToInt(AlignedLabel::getSuperPreferredWidth)
        .max()
        .orElse(0);
  }

  private int getSuperPreferredWidth() {
    return super.getPreferredSize().width;
  }

  public static void groupLabels(AlignedLabel... labels) {
    List<AlignedLabel> group = Arrays.asList(labels);
    group.forEach(al -> al.group = group);
  }
}
