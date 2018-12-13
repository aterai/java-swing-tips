// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();

    add(new JButton("JButton1"));
    add(new JButton("JButton2"));
    add(new MyButton("MyButton"));

    JButton button3 = new JButton("JButton3");
    button3.setName("green:3");
    add(button3);

    JButton button4 = new JButton("JButton4");
    button4.setName("green:4");
    add(button4);

    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    Class<?> clz = MainPanel.class;
    try (InputStream is = clz.getResourceAsStream("button.xml")) {
      SynthLookAndFeel synth = new SynthLookAndFeel();
      synth.load(is, clz);
      UIManager.setLookAndFeel(synth);
    } catch (IOException | ParseException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    // try {
    //   SynthLookAndFeel synth = new SynthLookAndFeel();
    //   synth.load(clz.getResource("button.xml"));
    //   UIManager.setLookAndFeel(synth);
    // } catch (IOException | ParseException | UnsupportedLookAndFeelException ex) {
    //   ex.printStackTrace();
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class MyButton extends JButton {
  protected MyButton(String title) {
    super(title + ": class");
  }
}
