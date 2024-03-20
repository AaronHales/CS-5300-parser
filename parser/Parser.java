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
    State closure = computeClosure(new Item( grammar.startRule, 0, "$"), grammar);
    System.out.println(closure.toString());
    states.addState(closure);

    boolean moreStates = true;
    System.out.println("starting while loop");
    while (moreStates) {
      moreStates = false;
      System.out.println("starting for i loop");
      for (int i = 0; i < states.size(); i++) {
        System.out.println("starting for each symbol loop");
        for (int j = 0; j < grammar.symbols.size(); j++) {
          State stateOnSymbol = GOTO(states.getState(i), grammar.symbols.get(j) ,grammar);
          System.out.println("stateOnSymbol: " + stateOnSymbol.toString());
          moreStates = moreStates || states.addState(stateOnSymbol);
        }
      }
    }
    System.out.println("States: " + states.toString());
  }

  public States getStates() {
    return states;
  }

  // TODO: Implement this method.
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

  static public void printClosure(State closure) {
    System.out.println(closure.toString());
  }

  // TODO: Implement this method.
  //   This returns a new state that represents the transition from
  //   the given state on the symbol X.
  static public State GOTO(State state, String X, Grammar grammar) {
    State ret = new State();
    for (int i = 0; i < state.size(); i++) {
      if (state.getItem(i).getNextSymbol() != null && state.getItem(i).getNextSymbol().equals(X)) {
        Item item = state.getItem(i);
        ret.addItem(item.advance());
      }
    }
    for (int i = 0; i < state.size(); i++) {
      Item item = state.getItem(i);
      State closure = computeClosure(item, grammar);
      for (int j = 0; j < closure.size(); j++) {
        ret.addItem(closure.getItem(j));
      }
    }
    return ret;
  }

  // TODO: Implement this method
  // You will want to use StringBuilder. Another useful method will be String.format: for
  // printing a value in the table, use
  //   String.format("%8s", value)
  // How much whitespace you have shouldn't matter with regard to the tests, but it will
  // help you debug if you can format it nicely.
  public String actionTableToString() {
    StringBuilder builder = new StringBuilder();
    return builder.toString();
  }

  // TODO: Implement this method
  // You will want to use StringBuilder. Another useful method will be String.format: for
  // printing a value in the table, use
  //   String.format("%8s", value)
  // How much whitespace you have shouldn't matter with regard to the tests, but it will
  // help you debug if you can format it nicely.
  public String gotoTableToString() {
    StringBuilder builder = new StringBuilder();
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
