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
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    box.add(Box.createHorizontalGlue());
    JButton start = makeButton("start");
    box.add(start);
    JButton pause = makeButton("pause");
    pause.setEnabled(false);
    box.add(pause);
    JButton reset = makeButton("reset");
    box.add(reset);

    SwingWorker<?, ?> worker = makePlayer(start, pause, reset);
    worker.execute();
    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        worker.cancel(true);
      }
    });
    add(makeTitle(getBackground()));
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static SwingWorker<?, ?> makePlayer(JButton start, JButton pause, JButton reset) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/Mozart_toruko_k.mid");
    return new AbstractPlayer(url) {
      private long tickPos;

      @Override protected void addListener(Sequencer sequencer) {
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

      @Override protected void process(List<Long> chunks) {
        chunks.forEach(tp -> {
          tickPos = tp;
          if (Objects.equals(tp, 0L)) {
            initButtons(true);
          }
        });
      }

      @Override protected void done() {
        tickPos = 0L;
        initButtons(true);
      }

      private void initButtons(boolean flg) {
        start.setEnabled(flg);
        pause.setEnabled(!flg);
      }
    };
  }

  private static Component makeTitle(Color bgc) {
    String txt = String.join(
        "\n",
        "Wolfgang Amadeus Mozart",
        "Piano Sonata No. 11 in A major, K 331",
        "(Turkish Rondo)");
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
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
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

abstract class AbstractPlayer extends SwingWorker<Void, Long> {
  private static final byte END_OF_TRACK = 0x2F;
  private final URL url;
  // private long tickPos;

  protected AbstractPlayer(URL url) {
    super();
    assert url != null;
    this.url = url;
  }

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
      publish(0L);
    }
    return null;
  }

  protected abstract void addListener(Sequencer sequencer);

  private void updateTickPosition(Sequencer sequencer) throws InterruptedException {
    if (sequencer.isRunning()) {
      publish(sequencer.getTickPosition());
    }
    Thread.sleep(1000L);
  }
}
