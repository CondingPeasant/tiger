package lexer;

import static control.Control.ConLexer.dump;

import java.io.InputStream;

import lexer.Token.Kind;

public class Lexer
{
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file
  int curLineNum;
  int lDelimiterNum;

  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
    curLineNum = 1;
    lDelimiterNum = 0;
  }

  // Discard all the characters going after "//" till end of the line.
  private void dealWithDoubleSlash() throws Exception {
    int c;
    do {
      c = this.fstream.read();
    } while ('\n' != c);
    curLineNum++;
  }

  // Discard all the characters between "/*" and "*/".
  // Delemiters can be nested.
  private void dealWithDelimiter() {
  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
    int c = this.fstream.read();
    if (-1 == c)
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, curLineNum);

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {
      if ('\n' == c)
        curLineNum++;
      c = this.fstream.read(); 
    }
    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, curLineNum);

    switch (c) {
    case '+':
      return new Token(Kind.TOKEN_ADD, curLineNum);
    case '&':
      c = this.fstream.read();
      if ('&' == c)
        return new Token(Kind.TOKEN_AND, curLineNum);
      else
        throw new Exception();
    case '=':
      return new Token(Kind.TOKEN_ASSIGN, curLineNum);
    case ',':
      return new Token(Kind.TOKEN_COMMER, curLineNum);
    case '.':
      return new Token(Kind.TOKEN_DOT, curLineNum);
    case '{':
      return new Token(Kind.TOKEN_LBRACE, curLineNum);
    case '[':
      return new Token(Kind.TOKEN_LBRACK, curLineNum);
    case '(':
      return new Token(Kind.TOKEN_LPAREN, curLineNum);
    case '<':
      return new Token(Kind.TOKEN_LT, curLineNum);
    case '!':
      return new Token(Kind.TOKEN_NOT, curLineNum);
    case '}':
      return new Token(Kind.TOKEN_RBRACE, curLineNum);
    case ']':
      return new Token(Kind.TOKEN_RBRACK, curLineNum);
    case ')':
      return new Token(Kind.TOKEN_RPAREN, curLineNum);
    case ';':
      return new Token(Kind.TOKEN_SEMI, curLineNum);
    case '-':
      return new Token(Kind.TOKEN_SUB, curLineNum);
    case '*':
      c = this.fstream.read();
      this.fstream.mark(1);
      if ('/' == c) {
        return new Token(Kind.TOKEN_RDELIMITER, curLineNum);
      } else {
        // roll back
        this.fstream.reset();
        return new Token(Kind.TOKEN_TIMES, curLineNum);
      }
    case '/':
      c = this.fstream.read();
      if ('/' == c) {
        Token token = new Token(Kind.TOKEN_DOUBLE_SLASH, curLineNum);
        dealWithDoubleSlash();
        return token;
      } else if ('*' == c) {
        Token token = new Token(Kind.TOKEN_RDELIMITER, curLineNum);
      } else {
        throw new Exception("After '/' is " + (char)c + ", at line" + curLineNum);
      }
    default:
      // Lab 1, exercise 2: supply missing code to
      // lex other kinds of tokens.
      // Hint: think carefully about the basic
      // data structure and algorithms. The code
      // is not that much and may be less than 50 lines. If you
      // find you are writing a lot of code, you
      // are on the wrong way.
      if (Character.isDigit(c)) {
        StringBuilder sb = new StringBuilder();
        do {
          sb.append((char)c);
          this.fstream.mark(1);
          c = this.fstream.read();
        }
        while(Character.isDigit(c));
        // rollback
        this.fstream.reset();
        if (sb.toString().matches("0|([1-9][0-9]*)")) {
          return new Token(Kind.TOKEN_NUM, curLineNum, sb.toString());
        } else {
          throw new Exception();
        }
      } else if (Character.isJavaIdentifierStart(c)) {
        StringBuilder sb = new StringBuilder();
        do {
          sb.append((char)c);
          this.fstream.mark(1);
          c = this.fstream.read();
        }
        while(Character.isJavaIdentifierPart(c));
        // rollback
        this.fstream.reset();
        if (sb.toString().equals("boolean")) {
          return new Token(Kind.TOKEN_BOOLEAN, curLineNum);
        } else if (sb.toString().equals("class")) {
          return new Token(Kind.TOKEN_CLASS, curLineNum);
        } else if (sb.toString().equals("else")) {
          return new Token(Kind.TOKEN_ELSE, curLineNum);
        } else if (sb.toString().equals("extends")) {
          return new Token(Kind.TOKEN_EXTENDS, curLineNum);
        } else if (sb.toString().equals("false")) {
          return new Token(Kind.TOKEN_FALSE, curLineNum);
        } else if (sb.toString().equals("if")) {
          return new Token(Kind.TOKEN_IF, curLineNum);
        } else if (sb.toString().equals("int")) {
          return new Token(Kind.TOKEN_INT, curLineNum);
        } else if (sb.toString().equals("length")) {
          return new Token(Kind.TOKEN_LENGTH, curLineNum);
        } else if (sb.toString().equals("main")) {
          return new Token(Kind.TOKEN_MAIN, curLineNum);
        } else if (sb.toString().equals("new")) {
          return new Token(Kind.TOKEN_NEW, curLineNum);
        } else if (sb.toString().equals("out")) {
          return new Token(Kind.TOKEN_OUT, curLineNum);
        } else if (sb.toString().equals("println")) {
          return new Token(Kind.TOKEN_PRINTLN, curLineNum);
        } else if (sb.toString().equals("public")) {
          return new Token(Kind.TOKEN_PUBLIC, curLineNum);
        } else if (sb.toString().equals("return")) {
          return new Token(Kind.TOKEN_RETURN, curLineNum);
        } else if (sb.toString().equals("static")) {
          return new Token(Kind.TOKEN_STATIC, curLineNum);
        } else if (sb.toString().equals("String")) {
          return new Token(Kind.TOKEN_STRING, curLineNum);
        } else if (sb.toString().equals("System")) {
          return new Token(Kind.TOKEN_SYSTEM, curLineNum);
        } else if (sb.toString().equals("this")) {
          return new Token(Kind.TOKEN_THIS, curLineNum);
        } else if (sb.toString().equals("true")) {
          return new Token(Kind.TOKEN_TRUE, curLineNum);
        } else if (sb.toString().equals("void")) {
          return new Token(Kind.TOKEN_VOID, curLineNum);
        } else if (sb.toString().equals("while")) {
          return new Token(Kind.TOKEN_WHILE, curLineNum);
        } else {
          return new Token(Kind.TOKEN_ID, curLineNum, sb.toString());
        }
      } else {
        throw new Exception("Current character is \"" + (char)c
                + "\", at line" + curLineNum);
      }
    }
  }

  public Token nextToken()
  {
    Token t = null;

    try {
      t = this.nextTokenInternal();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump)
      System.out.println(t.toString());
    return t;
  }
}
