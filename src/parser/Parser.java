package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser
{
  Lexer lexer;
  Token current, previous;

  public Parser(String fname, java.io.InputStream fstream)
  {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
    previous = null;
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.

  private void lookAhead()
  {
    previous = current;
    current = lexer.nextToken();
    //System.out.println("Look ahead\n" + current);
  }

  private void advance()
  {
    current = lexer.nextToken();
    //System.out.println(current);
  }

  private void eatToken(Kind kind)
  {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("At line " + current.lineNum + ", colum " + current.colNum);
      System.out.println("\tExpects: " + kind.toString());
      System.out.println("\tBut got: " + current.kind.toString());
      System.exit(1);
    }
  }

  private void error()
  {
    System.out.println("Syntax error: compilation aborting...");
    System.out.println("At line " + current.lineNum + ", colum " + current.colNum);
    System.exit(1);
    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private void parseExpList()
  {
    if (current.kind == Kind.TOKEN_RPAREN)
      return;
    parseExp();
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      parseExp();
    }
    return;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private void parseAtomExp()
  {
    switch (current.kind) {
    case TOKEN_LPAREN:
      advance();
      parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      return;
    case TOKEN_NUM:
      advance();
      return;
    case TOKEN_TRUE:
      advance();
      return;
    case TOKEN_FALSE:
      advance();
      return;
    case TOKEN_THIS:
      advance();
      return;
    case TOKEN_ID:
      advance();
      return;
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT:
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        return;
      case TOKEN_ID:
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        return;
      default:
        error();
        return;
      }
    }
    default:
      error();
      return;
    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private void parseNotExp()
  {
    parseAtomExp();
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          return;
        }
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
      } else {
        advance();
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
      }
    }
    return;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private void parseTimesExp()
  {
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
    }
    parseNotExp();
    return;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private void parseAddSubExp()
  {
    parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      parseTimesExp();
    }
    return;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private void parseLtExp()
  {
    parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      advance();
      parseAddSubExp();
    }
    return;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private void parseAndExp()
  {
    parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      parseLtExp();
    }
    return;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private void parseExp()
  {
    parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      parseAndExp();
    }
    return;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  private void parseStatement()
  {
    //System.out.println("In parseStatement(), previous<" + (previous == null ? "null" : previous) 
            //+ ">, current<" + current + ">");
    if (previous != null && previous.kind == Kind.TOKEN_ID) {
      if (current.kind == Kind.TOKEN_ASSIGN) {
        eatToken(Kind.TOKEN_ASSIGN);
        parseExp();
        eatToken(Kind.TOKEN_SEMI);
      } else if (current.kind == Kind.TOKEN_LBRACK) {
        eatToken(Kind.TOKEN_LBRACK);
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        eatToken(Kind.TOKEN_ASSIGN);
        parseExp();
        eatToken(Kind.TOKEN_SEMI);
      } else {
        error();
      }
      previous = null;
      return;
    }

    switch(current.kind) {
      case TOKEN_LBRACE:
        eatToken(Kind.TOKEN_LBRACE);
        if (current.kind == Kind.TOKEN_RBRACE) {
          eatToken(Kind.TOKEN_RBRACE);
        } else {
          parseStatements();
          eatToken(Kind.TOKEN_RBRACE);
        }
        return;
      case TOKEN_WHILE:
        eatToken(Kind.TOKEN_WHILE);
        eatToken(Kind.TOKEN_LPAREN);
        parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        parseStatements();
        return;
      case TOKEN_SYSTEM:
        eatToken(Kind.TOKEN_SYSTEM);
        eatToken(Kind.TOKEN_DOT);
        eatToken(Kind.TOKEN_OUT);
        eatToken(Kind.TOKEN_DOT);
        eatToken(Kind.TOKEN_PRINTLN);
        eatToken(Kind.TOKEN_LPAREN);
        parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_SEMI);
        return;
      case TOKEN_IF:
        eatToken(Kind.TOKEN_IF);
        eatToken(Kind.TOKEN_LPAREN);
        parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        parseStatements();
        eatToken(Kind.TOKEN_ELSE);
        parseStatements();
        return;
      case TOKEN_ID:
        eatToken(Kind.TOKEN_ID);
        if (current.kind == Kind.TOKEN_ASSIGN) {
          eatToken(Kind.TOKEN_ASSIGN);
          parseExp();
          eatToken(Kind.TOKEN_SEMI);
        } else if (current.kind == Kind.TOKEN_LBRACK) {
          eatToken(Kind.TOKEN_LBRACK);
          parseExp();
          eatToken(Kind.TOKEN_RBRACK);
          eatToken(Kind.TOKEN_ASSIGN);
          parseExp();
          eatToken(Kind.TOKEN_SEMI);
        } else {
          error();
        }
        return;
      default:
        error();
        return;
    }
  }

  // Statements -> Statement Statements
  // ->
  private void parseStatements()
  {
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM
        || current.kind == Kind.TOKEN_ID || (previous != null && previous.kind == Kind.TOKEN_ID)) {
      if (current.kind == Kind.TOKEN_ID) {
        lookAhead();
        if (current.kind == Kind.TOKEN_ID) {
          parseVarDecls();
        } else if (current.kind == Kind.TOKEN_ASSIGN || current.kind == Kind.TOKEN_LBRACK) {
          parseStatement();
        } else {
          error();
        }
      } else {
        parseStatement();
      }
    }
    return;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private void parseType()
  {
    //System.out.println("In parseType(), previous<" + (previous == null ? "null" : previous) 
            //+ ">, current<" + current + ">");
    if (previous != null && previous.kind == Kind.TOKEN_ID) {
      previous = null;
      return;
    }

    switch(current.kind) {
    case TOKEN_BOOLEAN:
      eatToken(Kind.TOKEN_BOOLEAN);
      return;
    case TOKEN_ID:
      eatToken(Kind.TOKEN_ID);
      return;
    case TOKEN_INT:
      eatToken(Kind.TOKEN_INT);
      if (current.kind == Kind.TOKEN_LBRACK) {
        eatToken(Kind.TOKEN_LBRACK);
        eatToken(Kind.TOKEN_RBRACK);
      }
      return;
    default:
      error();
      return;
    }
  }

  // VarDecl -> Type id ;
  private void parseVarDecl()
  {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    parseType();
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_SEMI);
    return;
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private void parseVarDecls()
  {
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN ||
        current.kind == Kind.TOKEN_ID || (previous != null && previous.kind == Kind.TOKEN_ID)) {
      if (current.kind == Kind.TOKEN_ID) {
        lookAhead();
        if (current.kind == Kind.TOKEN_ID) {
          parseVarDecl();
        } else if (current.kind == Kind.TOKEN_ASSIGN) {
          parseStatements();
        } else {
          error();
        }
      } else {
        parseVarDecl();
      }
    }
    return;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private void parseFormalList()
  {
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      parseType();
      eatToken(Kind.TOKEN_ID);
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        parseType();
        eatToken(Kind.TOKEN_ID);
      }
    }
    return;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private void parseMethod()
  {
    eatToken(Kind.TOKEN_PUBLIC);
    parseType();
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LPAREN);
    parseFormalList();
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    parseVarDecls();
    parseStatements();
    eatToken(Kind.TOKEN_RETURN);
    parseExp();
    eatToken(Kind.TOKEN_SEMI);
    eatToken(Kind.TOKEN_RBRACE);
    return;
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private void parseMethodDecls()
  {
    while (current.kind == Kind.TOKEN_PUBLIC) {
      parseMethod();
    }
    return;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private void parseClassDecl()
  {
    eatToken(Kind.TOKEN_CLASS);
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    parseVarDecls();
    parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    return;
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private void parseClassDecls()
  {
    while (current.kind == Kind.TOKEN_CLASS) {
      parseClassDecl();
    }
    return;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private void parseMainClass()
  {
    eatToken(Kind.TOKEN_CLASS);
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LBRACE);
    eatToken(Kind.TOKEN_PUBLIC);
    eatToken(Kind.TOKEN_STATIC);
    eatToken(Kind.TOKEN_VOID);
    eatToken(Kind.TOKEN_MAIN);
    eatToken(Kind.TOKEN_LPAREN);
    eatToken(Kind.TOKEN_STRING);
    eatToken(Kind.TOKEN_LBRACK);
    eatToken(Kind.TOKEN_RBRACK);
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    parseStatements();
    eatToken(Kind.TOKEN_RBRACE);
    eatToken(Kind.TOKEN_RBRACE);
  }

  // Program -> MainClass ClassDecl*
  private void parseProgram()
  {
    parseMainClass();
    parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return;
  }

  public void parse()
  {
    parseProgram();
    return;
  }
}
