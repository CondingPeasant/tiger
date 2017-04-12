package lexer;

public class Token
{
  // Lab 1, exercise 1: read the MiniJava specification
  // carefully, and answer these two questions:
  //   1. whether or not one should add other token kinds?
  //   2. which tokens come with an extra "lexeme", and
  //      which don't?
  // It's highly recommended that these token names are
  // alphabetically ordered, if you add new ones.
  public enum Kind {
    TOKEN_ADD, // "+"
    TOKEN_AND, // "&&"
    TOKEN_ASSIGN, // "="
    TOKEN_BOOLEAN, // "boolean"
    TOKEN_CLASS, // "class"
    TOKEN_COMMER, // ","
    TOKEN_DOT, // "."
    TOKEN_ELSE, // "else"
    TOKEN_EOF, // EOF
    TOKEN_EXTENDS, // "extends"
    TOKEN_FALSE, // "false"
    TOKEN_ID, // Identifier
    TOKEN_IF, // "if"
    TOKEN_INT, // "int"
    TOKEN_LBRACE, // "{"
    TOKEN_LBRACK, // "["
    TOKEN_LENGTH, // "length"
    TOKEN_LPAREN, // "("
    TOKEN_LT, // "<"
    TOKEN_MAIN, // "main"
    TOKEN_NEW, // "new"
    TOKEN_NOT, // "!"
    TOKEN_NUM, // IntegerLiteral
    // "out" is not a Java key word, but we treat it as
    // a MiniJava keyword, which will make the
    // compilation a little easier. Similar cases apply
    // for "println", "System" and "String".
    TOKEN_OUT, // "out"
    TOKEN_PRINTLN, // "println"
    TOKEN_PUBLIC, // "public"
    TOKEN_RBRACE, // "}"
    TOKEN_RBRACK, // "]"
    TOKEN_RETURN, // "return"
    TOKEN_RPAREN, // ")"
    TOKEN_SEMI, // ";"
    TOKEN_STATIC, // "static"
    TOKEN_STRING, // "String"
    TOKEN_SUB, // "-"
    TOKEN_SYSTEM, // "System"
    TOKEN_THIS, // "this"
    TOKEN_TIMES, // "*"
    TOKEN_TRUE, // "true"
    TOKEN_VOID, // "void"
    TOKEN_WHILE, // "while"
    // Tokens about comment will not be output.
    TOKEN_DOUBLE_SLASH, // "//"
    TOKEN_LDELIMITER, // "/*"
    TOKEN_RDELIMITER, // "*/"
  }

  public Kind kind; // kind of the token
  public String lexeme; // extra lexeme for this token, if any
  public Integer lineNum; // on which line of the source file this token appears
  public Integer colNum; // on which colum of the source file this token appears

  // Some tokens don't come with lexeme but 
  // others do.
  public Token(Kind kind, Integer lineNum, Integer colNum)
  {
    this.kind = kind;
    this.lineNum = lineNum;
    this.colNum = colNum;
 
    switch(kind) {
    case TOKEN_ADD: // "+"
    case TOKEN_ASSIGN: // "="
    case TOKEN_COMMER: // ","
    case TOKEN_DOT: // "."
    case TOKEN_LBRACE: // "{"
    case TOKEN_EOF: // EOF
    case TOKEN_LBRACK: // "["
    case TOKEN_LPAREN: // "("
    case TOKEN_LT: // "<"
    case TOKEN_NOT: // "!"
    case TOKEN_RBRACE: // "}"
    case TOKEN_RBRACK: // "]"
    case TOKEN_RPAREN: // ")"
    case TOKEN_SEMI: // ";"
    case TOKEN_TIMES: // "*"
    case TOKEN_SUB: // "-"
      colNum = new Integer(colNum + 1);
      break;
    case TOKEN_AND: // "&&"
    case TOKEN_DOUBLE_SLASH: // "//"
    case TOKEN_LDELIMITER: // "/*"
    case TOKEN_RDELIMITER: // "*/"
    case TOKEN_IF: // "if"
      colNum = new Integer(colNum + 2);
      break;
    case TOKEN_INT: // "int"
    case TOKEN_NEW: // "new"
    case TOKEN_OUT: // "out"
      colNum = new Integer(colNum + 3);
      break;
    case TOKEN_ELSE: // "else"
    case TOKEN_MAIN: // "main"
    case TOKEN_THIS: // "this"
    case TOKEN_TRUE: // "true"
    case TOKEN_VOID: // "void"
      colNum = new Integer(colNum + 4);
      break;
    case TOKEN_CLASS: // "class"
    case TOKEN_FALSE: // "false"
    case TOKEN_WHILE: // "while"
      colNum = new Integer(colNum + 5);
      break;
    case TOKEN_LENGTH: // "length"
    case TOKEN_PUBLIC: // "public"
    case TOKEN_RETURN: // "return"
    case TOKEN_STATIC: // "static"
    case TOKEN_STRING: // "String"
    case TOKEN_SYSTEM: // "System"
      colNum = new Integer(colNum + 6);
      break;
    case TOKEN_BOOLEAN: // "boolean"
    case TOKEN_EXTENDS: // "extends"
    case TOKEN_PRINTLN: // "println"
      colNum = new Integer(colNum + 7);
      break;
    case TOKEN_ID: // Identifier
    case TOKEN_NUM: // IntegerLiteral
    default:
      break;
    }
  }

  public Token(Kind kind, Integer lineNum, Integer colNum, String lexeme)
  {
    this(kind, lineNum, colNum);
    this.lexeme = lexeme;
    switch(kind) {
    case TOKEN_ID: // Identifier
    case TOKEN_NUM: // IntegerLiteral
      colNum = new Integer(colNum + lexeme.length());
      break;
    default:
      break;
    }
  }

  @Override
  public String toString()
  {
    String s;

    // to check that the "lineNum" field has been properly set.
    if (this.lineNum == null || this.colNum == null)
      new util.Todo();

    s = ": " + ((this.lexeme == null) ? "<NONE>" : this.lexeme) + " : at line "
        + this.lineNum.toString() + ", colum " + this.colNum.toString();
    return this.kind.toString() + s;
  }
}
