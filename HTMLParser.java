import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class HTMLParser {
  HTMLElement rootElement;

  static Pattern commentPattern;
  static Pattern doctypePattern;
  static Pattern rootTagPattern;
  static Pattern closingRootTagPattern;
  static Pattern selfClosingScriptPattern;
  static Pattern scriptPattern;
  static Pattern scriptClosingPattern;
  static Pattern tagPattern;
  static Pattern closingTagPattern;

  static final String commentRegex = "<!--.*-->\\z";
  static final String doctypeRegex = "<!doctype[^<>]*>\\z";
  static final String rootTagRegex = "<html[^<>]*>\\z";
  static final String closingRootTagRegex = "<\\/html\\s*>\\z";
  static final String selfClosingScriptRegex = "<script[^<>]*\\/\\s*>\\z";
  static final String scriptRegex = "<script[^<>]*>\\z";
  static final String scriptClosingRegex = "<\\/script\\s*>\\z";
  static final String tagRegex = "<[a-zA-Z][^<>]*>\\z";
  static final String closingTagRegex = "<\\/[a-zA-Z][^<>]*>\\z";

  // parse a fragment by adding filler code
  public void parseFragment(String htmlFragment) {
    // TODO: implement this
  }

  // determine whether the given stack contains a tagName element
  static boolean tagInStack(Vector<HTMLElement> stack, String tagName) {
    for(int i = 0; i < stack.size(); i++) {
      if(stack.get(i).tagName.equals(tagName)) {
        return true;
      }
    }
    return false;
  }

  HTMLParser() {
    rootElement = new HTMLElement("html");
  }

  static void compilePatterns() {
    commentPattern = Pattern.compile(commentRegex, Pattern.CASE_INSENSITIVE);
    doctypePattern = Pattern.compile(doctypeRegex, Pattern.CASE_INSENSITIVE);
    rootTagPattern = Pattern.compile(rootTagRegex, Pattern.CASE_INSENSITIVE);
    closingRootTagPattern = Pattern.compile(closingRootTagRegex, Pattern.CASE_INSENSITIVE);
    selfClosingScriptPattern = Pattern.compile(selfClosingScriptRegex, Pattern.CASE_INSENSITIVE);
    scriptPattern = Pattern.compile(scriptRegex, Pattern.CASE_INSENSITIVE);
    scriptClosingPattern = Pattern.compile(scriptClosingRegex, Pattern.CASE_INSENSITIVE);
    tagPattern = Pattern.compile(tagRegex, Pattern.CASE_INSENSITIVE);
    closingTagPattern = Pattern.compile(closingTagRegex, Pattern.CASE_INSENSITIVE);
  }

  // parse given HTML document into the rootElement
  public void parse(String htmlDoc) {
    String buffer = "";

    // declare and initialize the stack of open elements
    Vector<HTMLElement> stack = new Vector<HTMLElement>();
    stack.add(rootElement);

    char[] htmlDocChars = htmlDoc.toCharArray();

    compilePatterns();

    for(char ch : htmlDocChars) {
      buffer += ch;
      buffer = parseBuffer(buffer,stack);
    }
  }


  // parse a buffer text, return the new buffer (which may be an empty string)
  String parseBuffer(String buffer, Vector<HTMLElement> stack) {

    // short-circuit: return if the stack is empty
    if(stack.size() == 0) {
      return "";
    }

    // 1. check if we have finished a comment

    Matcher commentMatcher = commentPattern.matcher(buffer);
    if(commentMatcher.find()) {
      String comment = commentMatcher.group();
      String text = commentPattern.split(" "+buffer)[0].trim();
      if(text != "") {
        stack.lastElement().contents.add(new TextLeaf(text));
      }
      stack.lastElement().contents.add(new TextLeaf(comment));
      return "";
    }

    // 2. check if we are inside a comment (buffer includes "<!--")

    if(buffer.contains("<!--")) {
      return buffer;
    }

    // 3. check if we have defined doctype

    Matcher doctypeMatcher = doctypePattern.matcher(buffer);
    if(doctypeMatcher.find()) {
      return "";
    }

    // 4. check for root element

    Matcher rootTagMatcher = rootTagPattern.matcher(buffer);
    if(rootTagMatcher.find()) {
      rootElement.setAttributes(HTMLElement.createFromString(rootTagMatcher.group()));
      rootElement.contents.removeAllElements();
      stack.removeAllElements();
      stack.add(rootElement);
      return "";
    }

    // 5. check for closing root tag

    Matcher closingRootTagMatcher = closingRootTagPattern.matcher(buffer);
    if(closingRootTagMatcher.find()) {
      String text = closingRootTagPattern.split(" "+buffer)[0].trim();
      stack.lastElement().contents.add(new TextLeaf(text));
      stack.removeAllElements();
      return "";
    }

    // 6. check for self-closing script tag

    Matcher selfClosingScriptMatcher = selfClosingScriptPattern.matcher(buffer);
    if(selfClosingScriptMatcher.find()) {
      String text = selfClosingScriptPattern.split(" "+buffer)[0].trim();
      stack.lastElement().contents.add(new TextLeaf(text));
      stack.lastElement().contents.add(
          HTMLElement.createFromString(selfClosingScriptMatcher.group()));
      return "";
    }

    // 7. check for opening script tag

    Matcher scriptMatcher = scriptPattern.matcher(buffer);
    if(scriptMatcher.find()) {
      String text = scriptPattern.split(" "+buffer)[0].trim();
      stack.lastElement().contents.add(new TextLeaf(text));
      HTMLElement newElem = HTMLElement.createFromString(scriptMatcher.group());
      stack.lastElement().contents.add(newElem);
      stack.add(newElem);
      return "";
    }

    // 8. check for closing script tag

    Matcher scriptClosingMatcher = scriptClosingPattern.matcher(buffer);
    if(scriptClosingMatcher.find()) {
      String text = scriptClosingPattern.split(" "+buffer)[0].trim();
      stack.lastElement().contents.add(new TextLeaf(text));
      stack.remove(stack.size()-1);
      return "";
    }
    

    // 9. check if script is on the stack

    if(tagInStack(stack,"script")) {
      return buffer;
    }

    // 10. check for generic opening tag

    Matcher tagMatcher = tagPattern.matcher(buffer);
    if(tagMatcher.find()) {
      String text = tagPattern.split(" "+buffer)[0].trim();
      stack.lastElement().contents.add(new TextLeaf(text));
      handleOpeningTag(stack,tagMatcher.group());
      return "";
    }


    // 11. check for generic closing tag

    Matcher closingTagMatcher = closingTagPattern.matcher(buffer);
    if(closingTagMatcher.find()) {
      String text = closingTagPattern.split(" "+buffer)[0].trim();
      stack.lastElement().contents.add(new TextLeaf(text));
      handleClosingTag(stack,closingTagMatcher.group());
      return "";
    }

    return buffer;
  }

  // handle opening tag (push to stack)
  void handleOpeningTag(Vector<HTMLElement> stack, String openingTag) {
    HTMLElement elem = HTMLElement.createFromString(openingTag);
    Vector<HTMLElement> tmp = new Vector<HTMLElement>();

    if(Arrays.asList(HTMLElement.selfClosingTags()).contains(elem.tagName)) {
      stack.lastElement().contents.add(elem);
      return;
    }

    if( !Arrays.asList(HTMLElement.nestableTags()).contains( elem.tagName ) ) {
      while(tagInStack(stack,elem.tagName)) {
        if(Arrays.asList(HTMLElement.reopenableTags()).contains(
              stack.lastElement().tagName)) {
          tmp.add(new HTMLElement(stack.remove(stack.size()-1)));
        } else {
          stack.remove(stack.size()-1);
        }
      }

      while(tmp.size() > 0) {
        stack.add(tmp.remove(tmp.size()-1));
      }
    }

    stack.lastElement().contents.add(elem);
    stack.add(elem);
  }

  // handle closing tag (pop the stack)
  void handleClosingTag(Vector<HTMLElement> stack, String closingTag) {
    String tagName = closingTag.replace('<',' ').replace('>',' ').replace('/',' ').trim();
    Vector<HTMLElement> tmp = new Vector<HTMLElement>();

    if(!tagInStack(stack,tagName)) {
      return;
    }

    while(true) {
      if(stack.lastElement().tagName.equals(tagName)) {
        break;
      }
      if(Arrays.asList(HTMLElement.reopenableTags()).contains(
          stack.lastElement().tagName)) {
        tmp.add(new HTMLElement(stack.remove(stack.size()-1)));
      } else {
        stack.remove(stack.size()-1);
      }
    }

    stack.remove(stack.size()-1);

    while(tmp.size() > 0) {
      stack.add(tmp.remove(tmp.size()-1));
    }
  }



  //// DEBUGGING METHOD
  public void printDoc() {
    printElemAndContents(rootElement,0);
  }


  //// DEBUGGING METHOD
  void printElemAndContents(HTMLElement elem, int depth) {
    String tab = "    ";
    for(int i = 0; i < depth; i++) {
      System.out.print(tab);
    }
    System.out.println(elem.toHTML());

    for(int i = 0; i < elem.contents.size(); i++) {
      HTMLObject obj = elem.contents.get(i);
      if(obj.getClass().getName().equals("TextLeaf")) {
        if(obj.toHTML().trim() != "") {
          for(int j = 0; j < depth+1; j++) {
            System.out.print(tab);
          }
          System.out.println(obj.toHTML());
        }
      } else {
        printElemAndContents((HTMLElement) obj,depth+1);
      }
    }
  }
}
