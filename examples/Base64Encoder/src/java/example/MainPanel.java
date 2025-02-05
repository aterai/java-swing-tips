// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setBorder(BorderFactory.createTitledBorder("File -> String"));

    JLabel label = new JLabel("", SwingConstants.CENTER);
    label.setBorder(BorderFactory.createTitledBorder("JTextArea -> ImageIcon"));

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.add(scroll);
    p.add(label);

    JButton encode = new JButton("encode");
    encode.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
      int retValue = chooser.showOpenDialog(encode);
      if (retValue == JFileChooser.APPROVE_OPTION) {
        Path path = chooser.getSelectedFile().toPath();
        try {
          textArea.setText(Base64.getEncoder().encodeToString(Files.readAllBytes(path)));
        } catch (IOException ex) {
          textArea.setText("error: " + path);
        }
        // try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        //   BufferedImage image = ImageIO.read(file);
        //   ImageIO.write(image, "png", bos);
        //   bos.flush();
        //   // byte[] encoded = Base64.getEncoder().encode(bos.toByteArray());
        //   // textArea.setText(new String(encoded, StandardCharsets.ISO_8859_1));
        //   textArea.setText(Base64.getEncoder().encodeToString(bos.toByteArray()));
        // } catch (IOException ex) {
        //   textArea.setText("error: " + file.getAbsolutePath());
        // }
      }
    });
    JButton decode = new JButton("decode");
    decode.addActionListener(e -> {
      String b64 = textArea.getText();
      if (b64.isEmpty()) {
        return;
      }
      // byte[] bytes = b64.getBytes(StandardCharsets.ISO_8859_1);
      // try (InputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(bytes))) {
      try (InputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(b64))) {
        label.setIcon(new ImageIcon(ImageIO.read(input)));
      } catch (IOException ex) {
        ex.printStackTrace();
        label.setIcon(null);
      }
    });

    JPanel box = new JPanel(new GridLayout(1, 2, 5, 5));
    box.setBorder(BorderFactory.createTitledBorder("java.util.Base64"));
    box.add(encode);
    box.add(decode);

    add(box, BorderLayout.NORTH);
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
