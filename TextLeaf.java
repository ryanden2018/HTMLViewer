class TextLeaf extends HTMLObject {
  String text;

  TextLeaf(String text) {
    this.text = text;
  }

  public String toHTML() {
    return this.text;
  }
}

