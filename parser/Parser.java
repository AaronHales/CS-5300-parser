/*
 * Look for TODO comments in this file for suggestions on how to implement
 * your parser.
 */
package parser;

import java.io.IOException;
import java.util.*;

import lexer.ExprLexer;
import lexer.ParenLexer;
import lexer.SimpleLexer;
import lexer.TinyLexer;
import org.antlr.v4.runtime.*;

/**
 *
 */
public class Parser {

  final Grammar grammar;

  /**
   * All states in the parser.
   */
  private final States states;

  /**
   * Action table for bottom-up parsing. Accessed as
   * actionTable.get(state).get(terminal). You may replace
   * the Integer with a State class if you choose.
   */
  private final HashMap<Integer, HashMap<String, Action>> actionTable;
  /**
   * Goto table for bottom-up parsing. Accessed as gotoTable.get(state).get(nonterminal).
   * You may replace the Integers with State classes if you choose.
   */
  private final HashMap<Integer, HashMap<String, Integer>> gotoTable;

  public Parser(String grammarFilename) throws IOException {
    actionTable = new HashMap<>();
    gotoTable = new HashMap<>();

    grammar = new Grammar(grammarFilename);

    states = new States();

    // TODO: Call methods to compute the states and parsing tables here.
    State closure = computeClosure(new Item( grammar.startRule, 0, Util.EOF), grammar);
    states.addState(closure);

    boolean moreStates = true;
    System.out.println(grammar.symbols);
    while (moreStates) {
      moreStates = false;
      for (int i = 0; i < states.size(); i++) {
        for (String symbol : grammar.symbols) {
          State gotoOnSymbol = GOTO(states.getState(i), symbol, grammar);
          if (gotoOnSymbol.size() != 0) {
            moreStates = moreStates || states.addState(gotoOnSymbol);
          }
        }
      }
    }
    tableBuilder();
    System.out.println(actionTableToString());
    System.out.println(gotoTableToString());
  }

  private void tableBuilder() {
    for (int i = 0; i < states.size(); i++) {
      HashMap<String, Action> actionHash = new HashMap<>();
      HashMap<String, Integer> gotoHash = new HashMap<>();
      Action currentAction;
      for (int j = 0; j < states.getState(i).size(); j++) {
        Item currentItem = states.getState(i).getItem(j);
        if (currentItem.getNextSymbol() == null) {
          if (currentItem.getRule().equals(grammar.startRule) && currentItem.getLookahead().equals(Util.EOF)) {
            currentAction = Action.createAccept();
            actionHash.put(Util.EOF, currentAction);
          }
          else {
            currentAction = Action.createReduce(currentItem.getRule());
            actionHash.put(currentItem.getLookahead(), currentAction);
          }
        }
        else {
          State gotoFromState = GOTO(states.getState(i), currentItem.getNextSymbol(), grammar);
          for (int k = 0; k < states.size(); k++) {
            if (states.getState(k).equals(gotoFromState)) {
              if (grammar.isTerminal(currentItem.getNextSymbol())) {
                currentAction = Action.createShift(k);
                actionHash.put(currentItem.getNextSymbol(), currentAction);
              }
              else if (grammar.isNonterminal(currentItem.getNextSymbol())) {
                gotoHash.put(currentItem.getNextSymbol(), k);
              }
            }
          }
        }
      }
      actionTable.put(i, actionHash);
      gotoTable.put(i, gotoHash);
    }
  }

  public States getStates() {
    return states;
  }

  static public State computeClosure(Item I, Grammar grammar) {
    State closure = new State();
    closure.addItem(I);
    boolean moreItems = true;
    while (moreItems) {
      moreItems = false;
      for (int i = 0; i < closure.size(); i++) {
        for (Rule rule: grammar.rules) {
          if (rule.getLhs().equals(closure.getItem(i).getNextSymbol())) {
            String nextNextSymbol = closure.getItem(i).getNextNextSymbol();
            if (nextNextSymbol == null) {
              moreItems = moreItems || closure.addItem(new Item(rule, 0, closure.getItem(i).getA()));
            }
            else {
              if (grammar.isTerminal(nextNextSymbol)) {
                moreItems = moreItems || closure.addItem(new Item(rule, 0, nextNextSymbol));
              }
              else {
                HashSet<String> firstSymbols = grammar.first.get(nextNextSymbol);
                if (firstSymbols != null && !firstSymbols.isEmpty()) {
                  for(String item: grammar.first.get(nextNextSymbol)) {
                    moreItems = moreItems || closure.addItem(new Item(rule, 0, item));
                  }
                }
              }
            }
          }
        }
      }
    }
//    printClosure(closure);
    return closure;
  }

  static private void printClosure(State closure) {
    System.out.println(closure.toString());
  }

  static public State GOTO(State state, String X, Grammar grammar) {
    State ret = new State();
    for (int i = 0; i < state.size(); i++) {
      if (state.getItem(i).getNextSymbol() != null && state.getItem(i).getNextSymbol().equals(X)) {
        Item item = state.getItem(i);
        ret.addItem(item.advance());
      }
    }
    for (int i = 0; i < ret.size(); i++) {
      Item item = ret.getItem(i);
      State closure = computeClosure(item, grammar);
      for (int j = 0; j < closure.size(); j++) {
        ret.addItem(closure.getItem(j));
      }
    }
    return ret;
  }

  // You will want to use StringBuilder. Another useful method will be String.format: for
  // printing a value in the table, use
  //   String.format("%8s", value)
  // How much whitespace you have shouldn't matter with regard to the tests, but it will
  // help you debug if you can format it nicely.
  public String actionTableToString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("%11s", "state"));
    for (String terminal : grammar.terminals) {
      builder.append(String.format("%11s", terminal));
    }
    builder.append(String.format("%11s\n", Util.EOF));
    for (int stateNum : actionTable.keySet()) {
      builder.append(String.format("%11s", stateNum));
      for (String terminal : grammar.terminals) {
        if (actionTable.get(stateNum).get(terminal) == null) {
          builder.append(String.format("%11s", ""));
        }
        else {
          builder.append(String.format("%11s", actionTable.get(stateNum).get(terminal)));
        }
      }
      if (actionTable.get(stateNum).get(Util.EOF) == null) {
        builder.append(String.format("%11s", ""));
      }
      else {
        builder.append(String.format("%11s", actionTable.get(stateNum).get(Util.EOF)));
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  // You will want to use StringBuilder. Another useful method will be String.format: for
  // printing a value in the table, use
  //   String.format("%8s", value)
  // How much whitespace you have shouldn't matter with regard to the tests, but it will
  // help you debug if you can format it nicely.
  public String gotoTableToString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("%11s", "state"));
    for (String nonTerminal : grammar.nonterminals) {
      builder.append(String.format("%11s", nonTerminal));
    }
    builder.append("\n");
    for (int stateNum : gotoTable.keySet()) {
      builder.append(String.format("%11s", stateNum));
      for (String nonterminal : grammar.nonterminals) {
        if (gotoTable.get(stateNum).get(nonterminal) == null) {
          builder.append(String.format("%11s", ""));
        }
        else {
          builder.append(String.format("%11s", gotoTable.get(stateNum).get(nonterminal)));
        }
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  // TODO: Implement this method
  // You should return a list of the actions taken.
  public List<Action> parse(Lexer scanner) throws ParserException {
    // tokens is the output from the scanner. It is the list of tokens
    // scanned from the input file.
    // To get the token type: v.getSymbolicName(t.getType())
    // To get the token lexeme: t.getText()
    ArrayList<? extends Token> tokens = new ArrayList<>(scanner.getAllTokens());
    Vocabulary v = scanner.getVocabulary();

    Stack<String> input = new Stack<>();
    Collections.reverse(tokens);
    input.add(Util.EOF);
    for (Token t : tokens) {
      input.push(v.getSymbolicName(t.getType()));
    }
    Collections.reverse(tokens);
//    System.out.println(input);

    // TODO: Parse the tokens. On an error, throw a ParseException, like so:
    //    throw ParserException.create(tokens, i)
    List<Action> actions = new ArrayList<>();
    int pos = 0;
    try {
      Stack<Integer> stateStack = new Stack<>();
      stateStack.add(0);
      Stack<String> symbolStack = new Stack<>();
      String currentSymbol = input.pop();
      while (true) {
        System.out.printf("symbol Stack: %s\n", symbolStack);
        int topState = stateStack.peek();
        Action currentAction = actionTable.get(topState).get(currentSymbol);
        if (currentAction.isShift()) {
          stateStack.push(currentAction.getState());
          symbolStack.push(currentSymbol);
          currentSymbol = input.pop();
          pos++;
          actions.add(currentAction);
        }
        else if (currentAction.isReduce()) {
          Rule rule = actionTable.get(topState).get(currentSymbol).getRule();
          int size = rule.getRhs().size();
          for (int i = 0; i < size; i++) {
            stateStack.pop();
            symbolStack.pop();
          }
          stateStack.push(gotoTable.get(stateStack.peek()).get(rule.getLhs()));
          symbolStack.push(rule.getLhs());
          actions.add(currentAction);
        }
        else if (currentAction.isAccept()) {
          break;
        }
        else {
          throw ParserException.create(tokens, pos);
        }
      }
    }
    catch (Exception e) {
      throw ParserException.create(tokens, pos);
    }
    System.out.printf("actions: %s\n", actions);
    return actions;
  }

  //-------------------------------------------------------------------
  // Convenience functions
  //-------------------------------------------------------------------

  public List<Action> parseFromFile(String filename) throws IOException, ParserException {
//    System.out.println("\nReading input file " + filename + "\n");
    final CharStream charStream = CharStreams.fromFileName(filename);
    Lexer scanner = scanFile(charStream);
    return parse(scanner);
  }

  public List<Action> parseFromString(String program) throws ParserException {
    Lexer scanner = scanFile(CharStreams.fromString(program));
    return parse(scanner);
  }

  private Lexer scanFile(CharStream charStream) {
    // We use ANTLR's scanner (lexer) to produce the tokens.
    Lexer scanner = null;
    switch (grammar.grammarName) {
      case "Simple":
        scanner = new SimpleLexer(charStream);
        break;
      case "Paren":
        scanner = new ParenLexer(charStream);
        break;
      case "Expr":
        scanner = new ExprLexer(charStream);
        break;
      case "Tiny":
        scanner = new TinyLexer(charStream);
        break;
      default:
        System.out.println("Unknown scanner");
        break;
    }

    return scanner;
  }

}
