// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    String key1 = "ColorChooser.swatchesRecentSwatchSize";
    log.append(String.format("%s: %s%n", key1, UIManager.getDimension(key1)));
    String key2 = "ColorChooser.swatchesSwatchSize";
    log.append(String.format("%s: %s%n", key2, UIManager.getDimension(key2)));

    // UIManager.put("ColorChooser.swatchesDefaultRecentColor", Color.RED);
    UIManager.put(key1, new Dimension(10, 8));
    UIManager.put(key2, new Dimension(6, 10));

    JButton button1 = new JButton("JColorChooser.showDialog(...)");
    button1.addActionListener(e -> {
      Color color = JColorChooser.showDialog(getRootPane(), "JColorChooser", null);
      if (color != null) {
        log.append(String.format("color: %s%n", color));
      }
    });

    JColorChooser cc = new JColorChooser();
    ColorTracker ok = new ColorTracker(cc);
    ActionListener cancel = e -> log.append("cancel\n");
    String title = "ColorChooserSwatchSize";
    JDialog dialog = JColorChooser.createDialog(getRootPane(), title, true, cc, ok, cancel);
    JButton button2 = new JButton("JColorChooser.createDialog(...).setVisible(true)");
    button2.addActionListener(e -> {
      // dialog.setSize(320, 240);
      dialog.setVisible(true);
      Color color = ok.getColor();
      if (color != null) {
        log.append(String.format("color: %s%n", color));
      }
    });

    // JButton serialize = new JButton("serialize");
    // serialize.addActionListener(e -> {
    //   try (FileOutputStream fos = new FileOutputStream("color.dat");
    //        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
    //     oos.writeObject(cc);
    //     oos.flush();
    //   } catch (IOException ex) {
    //     ex.printStackTrace();
    //   }
    // });

    // JButton deserialize = new JButton("deserialize");
    // deserialize.addActionListener(e -> {
    //   try (FileInputStream fis = new FileInputStream("color.dat");
    //        ObjectInputStream ois = new ObjectInputStream(fis)) {
    //     JColorChooser jcc = (JColorChooser) ois.readObject();
    //     JDialog d = JColorChooser.createDialog(
    //         getRootPane(), "title", true, jcc, null, null);
    //     d.setVisible(true);
    //   } catch (IOException | ClassNotFoundException ex) {
    //     ex.printStackTrace();
    //   }
    // });

    JPanel p = new JPanel(new GridLayout(2, 1, 10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.NORTH);
    // add(serialize);
    // add(deserialize);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
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

class ColorTracker implements ActionListener {
  private final JColorChooser chooser;
  private Color color;

  protected ColorTracker(JColorChooser c) {
    chooser = c;
  }

  @Override public void actionPerformed(ActionEvent e) {
    color = chooser.getColor();
  }

  public Color getColor() {
    return color;
  }
}
