package lexer;

import static control.Control.ConLexer.dump;

import java.io.InputStream;

import lexer.Token.Kind;

public class Lexer
{
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file
  Integer curLineNum;
  Integer curColNum;
  int lDelimiterNum;
  boolean isAfterDoubleSlash;

  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
    curLineNum = 1;
    curColNum = 0;
    lDelimiterNum = 0;
    isAfterDoubleSlash = false;
  }

  // Discard all the characters going after "//" till end of the line.
  private void dealWithDoubleSlash() throws Exception {
    int c;
    do {
      c = this.fstream.read();
    } while ('\n' != c);
    curLineNum++;
    curColNum = 0;
    isAfterDoubleSlash = false;
  }

  // Discard all the characters between "/*" and "*/".
  // Delemiters can be nested.
  private Token dealWithDelimiter() {
    Token token = null;
    // omit all tokens except "/*", "*/" and EOF
    do {
      try {
        token = nextTokenInternal();
      } catch (Exception e) {
        token = null;
      }
    } while (null == token || 
            (Kind.TOKEN_LDELIMITER != token.kind 
            && Kind.TOKEN_RDELIMITER != token.kind
            && Kind.TOKEN_EOF != token.kind));
    return token;
  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
    Token token;
    int c = this.fstream.read();
    curColNum++;

    if (-1 == c)
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, curLineNum, curColNum);

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {
      if ('\n' == c) {
        curLineNum++;
        curColNum = 0;
      }
      c = this.fstream.read(); 
      curColNum++;
      // How to deal with \t ???
    }
    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, curLineNum, curColNum);

    switch (c) {
    case '+':
      token = new Token(Kind.TOKEN_ADD, curLineNum, curColNum);
      return token;
    case '&':
      c = this.fstream.read();
      if ('&' == c) {
        token = new Token(Kind.TOKEN_AND, curLineNum, curColNum);
        curColNum += 1;
        return token;
      } else {
        curColNum++;
        throw new Exception();
      }
    case '=':
      token = new Token(Kind.TOKEN_ASSIGN, curLineNum, curColNum);
      return token;
    case ',':
      token = new Token(Kind.TOKEN_COMMER, curLineNum, curColNum);
      return token;
    case '.':
      token = new Token(Kind.TOKEN_DOT, curLineNum, curColNum);
      return token;
    case '{':
      token = new Token(Kind.TOKEN_LBRACE, curLineNum, curColNum);
      return token;
    case '[':
      token = new Token(Kind.TOKEN_LBRACK, curLineNum, curColNum);
      return token;
    case '(':
      token = new Token(Kind.TOKEN_LPAREN, curLineNum, curColNum);
      return token;
    case '<':
      token = new Token(Kind.TOKEN_LT, curLineNum, curColNum);
      return token;
    case '!':
      token = new Token(Kind.TOKEN_NOT, curLineNum, curColNum);
      return token;
    case '}':
      token = new Token(Kind.TOKEN_RBRACE, curLineNum, curColNum);
      return token;
    case ']':
      token = new Token(Kind.TOKEN_RBRACK, curLineNum, curColNum);
      return token;
    case ')':
      token = new Token(Kind.TOKEN_RPAREN, curLineNum, curColNum);
      return token;
    case ';':
      token = new Token(Kind.TOKEN_SEMI, curLineNum, curColNum);
      return token;
    case '-':
      token = new Token(Kind.TOKEN_SUB, curLineNum, curColNum);
      return token;
    case '*':
      c = this.fstream.read();
      this.fstream.mark(1);
      if ('/' == c) {
        token = new Token(Kind.TOKEN_RDELIMITER, curLineNum, curColNum);
        curColNum += 1;
        return token;
      } else {
        // roll back
        this.fstream.reset();
        token = new Token(Kind.TOKEN_TIMES, curLineNum, curColNum);
        return token;
      }
    case '/':
      c = this.fstream.read();
      if ('/' == c) {
        token = new Token(Kind.TOKEN_DOUBLE_SLASH, curLineNum, curColNum);
        curColNum += 1;
        return token;
      } else if ('*' == c) {
        token = new Token(Kind.TOKEN_LDELIMITER, curLineNum, curColNum);
        curColNum += 1;
        return token;
      } else {
        throw new Exception("After '/' is " + (char)c 
                + ", at line" + curLineNum + ", colum " + curColNum);
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
          token = new Token(Kind.TOKEN_NUM, curLineNum, curColNum, sb.toString());
          curColNum += sb.toString().length() - 1;
          return token;
        } else {
          curColNum += sb.toString().length() - 1;
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
          token = new Token(Kind.TOKEN_BOOLEAN, curLineNum, curColNum);
          curColNum += 6;
          return token;
        } else if (sb.toString().equals("class")) {
          token = new Token(Kind.TOKEN_CLASS, curLineNum, curColNum);
          curColNum += 4;
          return token;
        } else if (sb.toString().equals("else")) {
          token = new Token(Kind.TOKEN_ELSE, curLineNum, curColNum);
          curColNum += 3;
          return token;
        } else if (sb.toString().equals("extends")) {
          token = new Token(Kind.TOKEN_EXTENDS, curLineNum, curColNum);
          curColNum += 6;
          return token;
        } else if (sb.toString().equals("false")) {
          token = new Token(Kind.TOKEN_FALSE, curLineNum, curColNum);
          curColNum += 4;
          return token;
        } else if (sb.toString().equals("if")) {
          token = new Token(Kind.TOKEN_IF, curLineNum, curColNum);
          curColNum += 1;
          return token;
        } else if (sb.toString().equals("int")) {
          token = new Token(Kind.TOKEN_INT, curLineNum, curColNum);
          curColNum += 3;
          return token;
        } else if (sb.toString().equals("length")) {
          token = new Token(Kind.TOKEN_LENGTH, curLineNum, curColNum);
          curColNum += 5;
          return token;
        } else if (sb.toString().equals("main")) {
          token = new Token(Kind.TOKEN_MAIN, curLineNum, curColNum);
          curColNum += 3;
          return token;
        } else if (sb.toString().equals("new")) {
          token = new Token(Kind.TOKEN_NEW, curLineNum, curColNum);
          curColNum += 2;
          return token;
        } else if (sb.toString().equals("out")) {
          token = new Token(Kind.TOKEN_OUT, curLineNum, curColNum);
          curColNum += 2;
          return token;
        } else if (sb.toString().equals("println")) {
          token = new Token(Kind.TOKEN_PRINTLN, curLineNum, curColNum);
          curColNum += 6;
          return token;
        } else if (sb.toString().equals("public")) {
          token = new Token(Kind.TOKEN_PUBLIC, curLineNum, curColNum);
          curColNum += 5;
          return token;
        } else if (sb.toString().equals("return")) {
          token = new Token(Kind.TOKEN_RETURN, curLineNum, curColNum);
          curColNum += 5;
          return token;
        } else if (sb.toString().equals("static")) {
          token = new Token(Kind.TOKEN_STATIC, curLineNum, curColNum);
          curColNum += 5;
          return token;
        } else if (sb.toString().equals("String")) {
          token = new Token(Kind.TOKEN_STRING, curLineNum, curColNum);
          curColNum += 5;
          return token;
        } else if (sb.toString().equals("System")) {
          token = new Token(Kind.TOKEN_SYSTEM, curLineNum, curColNum);
          curColNum += 5;
          return token;
        } else if (sb.toString().equals("this")) {
          token = new Token(Kind.TOKEN_THIS, curLineNum, curColNum);
          curColNum += 3;
          return token;
        } else if (sb.toString().equals("true")) {
          token = new Token(Kind.TOKEN_TRUE, curLineNum, curColNum);
          curColNum += 3;
          return token;
        } else if (sb.toString().equals("void")) {
          token = new Token(Kind.TOKEN_VOID, curLineNum, curColNum);
          curColNum += 3;
          return token;
        } else if (sb.toString().equals("while")) {
          token = new Token(Kind.TOKEN_WHILE, curLineNum, curColNum);
          curColNum += 4;
          return token;
        } else {
          token = new Token(Kind.TOKEN_ID, curLineNum, curColNum, sb.toString());
          curColNum += sb.toString().length() - 1;
          return token;
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

    if (isAfterDoubleSlash) {
      try {
        dealWithDoubleSlash();
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    if (lDelimiterNum > 0) {
      t = dealWithDelimiter();
    } else {
      try {
        t = this.nextTokenInternal();
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    // Suppress the output of double slash and delimiter.
    if (Kind.TOKEN_LDELIMITER == t.kind) {
      lDelimiterNum++;
      return nextToken();
    } else if (Kind.TOKEN_RDELIMITER == t.kind) {
      lDelimiterNum--;
      return nextToken();
    } else if (Kind.TOKEN_DOUBLE_SLASH == t.kind) {
      isAfterDoubleSlash = true;
      return nextToken();
    }

    if (dump)
      System.out.println(t.toString());
    return t;
  }
}
