// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final byte END_OF_TRACK = 0x2F;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JButton start = makeButton("start");
    JButton pause = makeButton("pause");
    pause.setEnabled(false);
    JButton reset = makeButton("reset");

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/Mozart_toruko_k.mid");
    assert url != null;
    SwingWorker<Void, Long> worker = new SwingWorker<Void, Long>() {
      private long tickPos;
      @Override protected Void doInBackground() throws InterruptedException {
        try (Sequencer sequencer = MidiSystem.getSequencer()) {
          sequencer.open();
          sequencer.setSequence(MidiSystem.getSequence(url));
          sequencer.addMetaEventListener(e -> {
            if (e.getType() == END_OF_TRACK) {
              publish(0L);
            }
          });
          EventQueue.invokeLater(() -> addListener(sequencer));
          while (sequencer.isOpen()) {
            updateTickPosition(sequencer);
          }
        } catch (InvalidMidiDataException | MidiUnavailableException | IOException ex) {
          ex.printStackTrace();
          publish(0L);
        }
        return null;
      }

      private void addListener(Sequencer sequencer) {
        start.addActionListener(e -> {
          sequencer.setTickPosition(tickPos);
          sequencer.start();
          initButtons(false);
        });

        pause.addActionListener(e -> {
          publish(sequencer.getTickPosition());
          sequencer.stop();
          initButtons(true);
        });

        reset.addActionListener(e -> {
          sequencer.stop();
          tickPos = 0L;
          initButtons(true);
        });
      }

      private void updateTickPosition(Sequencer sequencer) throws InterruptedException {
        if (sequencer.isRunning()) {
          publish(sequencer.getTickPosition());
        }
        Thread.sleep(1000L);
      }

      @Override protected void process(List<Long> chunks) {
        chunks.forEach(tp -> {
          tickPos = tp;
          if (Objects.equals(tp, 0L)) {
            initButtons(true);
          }
        });
      }

      // @Override protected void done() {
      //   tickPos = 0L;
      //   initButtons(true);
      // }

      private void initButtons(boolean flg) {
        start.setEnabled(flg);
        pause.setEnabled(!flg);
      }
    };
    worker.execute();

    addHierarchyListener(worker);

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    box.add(Box.createHorizontalGlue());
    box.add(start);
    box.add(pause);
    box.add(reset);

    add(makeTitle(getBackground()));
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void addHierarchyListener(SwingWorker<?, ?> worker) {
    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        // System.out.println("DISPLAYABILITY_CHANGED");
        worker.cancel(true);
      }
    });
  }

  private static Component makeTitle(Color bgc) {
    String txt = "Wolfgang Amadeus Mozart\nPiano Sonata No. 11 in A major, K 331\n(Turkish Rondo)";
    JTextArea label = new JTextArea(txt);
    label.setBorder(BorderFactory.createTitledBorder("MIDI"));
    label.setEditable(false);
    label.setBackground(bgc);
    return label;
  }

  private static JButton makeButton(String title) {
    JButton b = new JButton(title);
    b.setFocusable(false);
    return b;
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
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
