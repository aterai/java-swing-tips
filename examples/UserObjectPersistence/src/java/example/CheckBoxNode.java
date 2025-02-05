// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

public final class CheckBoxNode {
  private final String label;
  private final Status status;

  public CheckBoxNode() {
    this.label = "";
    this.status = Status.INDETERMINATE;
  }

  public CheckBoxNode(String label) {
    this.label = label;
    this.status = Status.INDETERMINATE;
  }

  public CheckBoxNode(String label, Status status) {
    this.label = label;
    this.status = status;
  }

  // public void setLabel(String label) {
  //   this.label = label;
  // }

  public String getLabel() {
    return label;
  }

  // public void setStatus(Status status) {
  //   this.status = status;
  // }

  public Status getStatus() {
    return status;
  }

  @Override public String toString() {
    return label;
  }
}
