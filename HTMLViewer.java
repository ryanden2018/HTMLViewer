class HTMLViewer {

  public static void main(String[] args) {
    HTMLElement elem = HTMLElement.createFromString("<input type='submit' placeholder='Some Stuff'>");
    System.out.println(elem.toHTML());
    System.out.println((new HTMLElement(elem)).toHTML());
    System.out.println(HTMLElement.createFromString("<a href=\"place.html\" target=_blank>").toHTML());
    System.out.println(HTMLElement.createFromString("<br />").toHTML());
    System.out.println(HTMLElement.createFromString("<br/>").toHTML());
    System.out.println(HTMLElement.createFromString("<br>").toHTML());
    System.out.println(HTMLElement.createFromString("<img src='place.jpg' />").toHTML());
    System.out.println(HTMLElement.createFromString("<img src='no.jpg' style=\"myspace\"/>").toHTML());
  }
}
