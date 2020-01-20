// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    System.out.println(UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize"));
    System.out.println(UIManager.getDimension("ColorChooser.swatchesSwatchSize"));

    // UIManager.put("ColorChooser.swatchesDefaultRecentColor", Color.RED);
    UIManager.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(10, 8));
    UIManager.put("ColorChooser.swatchesSwatchSize", new Dimension(6, 10));

    JButton button1 = new JButton("JColorChooser.showDialog(...)");
    button1.addActionListener(e -> {
      Color color = JColorChooser.showDialog(getRootPane(), "JColorChooser", null);
      System.out.println(color);
    });

    JColorChooser cc = new JColorChooser();
    JDialog dialog = JColorChooser.createDialog(
        getRootPane(), "JST ColorChooserSwatchSize", true, cc,
        e -> System.out.println("ok"),
        e -> System.out.println("cancel"));
    JButton button2 = new JButton("JColorChooser.createDialog(...).setVisible(true)");
    button2.addActionListener(e -> {
      // dialog.setSize(320, 240);
      dialog.setVisible(true);
      Color color = cc.getColor();
      System.out.println(color);
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

    add(button1);
    add(button2);
    // add(serialize);
    // add(deserialize);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
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
