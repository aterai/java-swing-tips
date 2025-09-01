package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class OneLineTextPane extends JTextPane {
  @Override public final void updateUI() {
    super.updateUI();
    String key = "Do-Nothing";
    InputMap im = getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), key);
    getActionMap().put(key, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        // Do nothing
      }
    });
    setEditorKit(new NoWrapEditorKit());
    enableInputMethods(false);
  }

  @Override public final void scrollRectToVisible(Rectangle rect) {
    rect.grow(getInsets().right, 0);
    super.scrollRectToVisible(rect);
  }
}

class NoWrapEditorKit extends StyledEditorKit {
  @Override public ViewFactory getViewFactory() {
    return new NoWrapViewFactory();
  }
}

class NoWrapParagraphView extends ParagraphView {
  protected NoWrapParagraphView(Element elem) {
    super(elem);
  }

  @Override protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    SizeRequirements req = super.calculateMinorAxisRequirements(axis, r);
    req.minimum = req.preferred;
    return req;
  }

  @Override public int getFlowSpan(int index) {
    return Integer.MAX_VALUE;
  }
}

class NoWrapViewFactory implements ViewFactory {
  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public View create(Element elem) {
    switch (elem.getName()) {
      case AbstractDocument.ParagraphElementName:
        return new NoWrapParagraphView(elem);
      case AbstractDocument.SectionElementName:
        return new BoxView(elem, View.Y_AXIS);
      case StyleConstants.ComponentElementName:
        return new ComponentView(elem);
      case StyleConstants.IconElementName:
        return new IconView(elem);
      default:
        return new LabelView(elem);
    }
  }
}
