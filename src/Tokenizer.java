import java.io.*;
import java.util.*;

/**
 * Tokenizer class for C. Takes in a file and parses the tokens. the nextToken method
 * will return the next token to the user. Main method accepts file as command line
 * argument. If no file is provided, the supplied CFile.c will be parsed. In it, there
 * is C code to show how the breakdown of tokens. Main method will print each Token type
 * followed by the lexeme for that token.
 *
 * Create by Avi Amon
 */
public class Tokenizer {
    private Stack<Character> stack;
    private BufferedReader br;
    private Map<String, Token.Type> keywordTypes;
    private Set<Character> punctuators;
    private Set<Character> hexSet;

    /**
     * Constructs a Tokenizer
     * @param pathToFile The path of the C file.
     */
    public Tokenizer(String pathToFile){
        this.stack = new Stack<>();
        try{
            this.br = new BufferedReader(new FileReader(new File(pathToFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initKeywordMap();
        initPuncSet();
        initHexSet();
    }

    /**
     * Returns the next token in the C file.
     * @return the next token in the C file
     */
    public Token nextToken(){
        char c;
        int i = 0;
        char token[] = new char[10000];
        Token tk;

        skipWhiteSpaces();

        //if at end of input stream
        c = getChar();
        if((int) c == 0){
            return new Token("EOF".toCharArray(), Token.Type.END);
        }

        token[i++] = c;

        //check for integer or float
        tk = checkConstantNumbers(token, c, i);
        if(tk != null){
            return tk;
        }

        //check for string constant
        tk = checkConstantString(token, c, i);
        if(tk != null){
            return tk;
        }

        //check for char constant
        tk = checkConstantChar(token, c, i);
        if(tk != null){
            return tk;
        }

        //check for  block comment
        tk = checkBlockComment(token);
        if(tk != null){
            return tk;
        }

        //check for line comment
        tk = checkLineComment(token);
        if(tk != null){
            return tk;
        }

        //check for keyword
        tk = checkKeyword(token);
        if(tk != null){
            return tk;
        }

        //check for punctuators
        tk = checkPunctuators(token);
        if(tk != null){
            return tk;
        }

        //check for identifier
        tk = checkIdentifier(token);
        if(tk != null) {
            return tk;
        }

        return null;
    }

    /**
     * Determines if the next token is a number constant.
     * @param token the token char array which that will contain the token.
     * @param c the current character being looked at.
     * @param i the index used for token char array.
     * @return a number token depending on the type, or null if not number constant
     */
    private Token checkConstantNumbers(char[] token, char c, int i){
        int state;
        if(Character.isDigit(c) || c == '.'){
            if(c == '.'){
                state = 4;
                c = getChar();
                char d = getChar();
                boolean variadic = c == '.' && d == '.';
                if(variadic){
                    return new Token("...".toCharArray(), Token.Type.VARIADIC);
                }
                else if(!Character.isDigit(c)){
                    ungetChar(d);
                    ungetChar(c);
                    return new Token(".".toCharArray(), Token.Type.PERIOD);
                }
                ungetChar(d);
                ungetChar(c);
            }
            else {
                char d = getChar();
                if(c == '0' && Character.isDigit(d)){
                    state = 9;
                }
                else {
                    state = 2;
                }
                ungetChar(d);
            }

            c = getChar();
            while(state > 0){
                switch (state) {
                    case 2:
                        if (Character.isDigit(c))
                            state = 2;
                        else if (c == 'e' || c == 'E')
                            state = 5;
                        else if (c == '.')
                            state = 3;
                        else if (isIntSuffix(c)) {
                            state = 12;
                        }
                        else
                            state = -2;
                        break;
                    case 3:
                        if (Character.isDigit(c))
                            state = 8;
                        else
                            state = -3;
                        break;
                    case 4:
                        if (Character.isDigit(c))
                            state = 3;
                        else
                            state = -4;
                        break;
                    case 5:
                        if (Character.isDigit(c))
                            state = 6;
                        else if (c == '-' || c == '+')
                            state = 7;
                        else
                            state = -5;
                        break;
                    case 6:
                        if (Character.isDigit(c))
                            state = 6;
                        else if (isFloatSuffix(c)){
                            state = 19;
                        }
                        else
                            state = -6;
                        break;
                    case 7:
                        if(Character.isDigit(c))
                            state = 6;
                        else
                            state = -7;
                        break;
                    case 8:
                        if(Character.isDigit(c))
                            state = 8;
                        else if(c == 'e' || c == 'E')
                            state = 5;
                        else if(isFloatSuffix(c)) {
                            state = 19;
                        }
                        else
                            state = -8;
                        break;
                    case 9:
                        if(c == 'x' || c == 'X'){
                            state = 11;
                        }
                        else{
                            state = 10;
                        }
                        break;
                    case 10:
                        int num = c - '0';
                        if(Character.isDigit(c) && num < 7)
                            state = 10;
                        else if(isIntSuffix(c)){
                            state = 12;
                        }
                        else
                            state = -10;
                        break;
                    case 11:
                        if(Character.isDigit(c) || this.hexSet.contains(c))
                            state = 11;
                        else if(isIntSuffix(c)){
                            state = 12;
                        }
                        else
                            state = -11;
                        break;
                    case 12:
                        if(c == 'U')
                            state = 13;
                        else if(c == 'u')
                            state = 14;
                        else if(c == 'L')
                            state = 15;
                        else if(c == 'l')
                            state = 16;
                        else if(this.punctuators.contains(c))
                            state = -2;
                        else
                            state = -12;
                        break;
                    case 13:
                        if(c == 'L')
                            state = 15;
                        else if(this.punctuators.contains(c))
                            state = -2;
                        else
                            state = -12;
                        break;
                    case 14:
                        if(c == 'l')
                            state = 16;
                        else if(this.punctuators.contains(c))
                            state = -2;
                        else
                            state = -12;
                        break;
                    case 15:
                        if(c == 'L')
                            state = 17;
                        else if(this.punctuators.contains(c))
                            state = -2;
                        else
                            state = -12;
                        break;
                    case 16:
                        if(c == 'l')
                            state = 18;
                        else if(this.punctuators.contains(c))
                            state = -2;
                        else
                            state = -12;
                        break;
                    case 17:
                        state = -2;
                        break;
                    case 18:
                        if(this.punctuators.contains(c))
                            state = -2;
                        else
                            state = -12;
                        break;
                    case 19:
                        if(this.punctuators.contains(c)){
                            state = -6;
                        }
                        else
                            state = -12;
                        break;
                    case 20:
                        state = -6;
                        break;
                }
                if (state > 0) {
                    token[i++] = c;
                    token[i] = '\0';
                    c = getChar();
                }
            }
            ungetChar(c);
            switch(-state){
                case 2:
                    return new Token(token, Token.Type.INTEGER_CONSTANT);
                //CLion IDE says that "1." format in case -3 turns number to zero. Online gcc compilers just make
                //it an integer, usually a very small negative. I give it the lexeme 0 with INTEGER_CONSTANT as token.
                case 3:
                    return new Token("0".toCharArray(), Token.Type.INTEGER_CONSTANT);
                case 4:
                    return new Token(token, Token.Type.PERIOD);
                case 6:
                    return new Token(token, Token.Type.FLOAT_CONSTANT);
                case 8:
                    return new Token(token, Token.Type.FLOAT_CONSTANT);
                case 10:
                    return new Token(token, Token.Type.BAD_OCTAL);
                case 11:
                    return new Token(token, Token.Type.BAD_HEX);
                case 12:
                    return new Token(token, Token.Type.BAD_SUFFIX_TOKEN);
                default:
                    return new Token(token, Token.Type.BAD_FLOAT);
            }
        }
        return null;
    }

    /**
     * Determines if the next token is a string constant.
     * @param token the token char array which that will contain the token.
     * @param c the current character being looked at.
     * @param i the index used for token char array.
     * @return a string token, or null if not string constant
     */
    private Token checkConstantString(char[] token, char c, int i){
        if(c == '"'){
            boolean unbalancedQuote = false;
            while((c = getChar()) != '"' ||
                    (token[i - 1] == '\\' && token[i - 2] != '\\')){
                if(c =='"' || c == 0){
                    unbalancedQuote = true;
                }
                else{
                    unbalancedQuote = false;
                }
                if((int) c == 0 )
                    break;
                token[i++] = c;
            }
            token[i] = c;

            if(c == '"'){
                unbalancedQuote = false;
            }

            if(unbalancedQuote)
                return new Token(token, Token.Type.BAD_STRING);

            return new Token(token, Token.Type.STRING_CONSTANT);
        }
        return null;
    }

    /**
     * Determines if the next token is a character constant.
     * @param token the token char array which that will contain the token.
     * @param c the current character being looked at.
     * @param i the index used for token char array.
     * @return a character token, or null if not character constant
     */
    private Token checkConstantChar(char[] token, char c, int i){
        if(c == '\''){
            boolean unbalancedSingleQuote = false;
            while((c = getChar()) != '\'' ||
                    (token[i - 1] == '\\' && token[i - 2] != '\\')){
                if(c =='\'' || c == 0){
                    unbalancedSingleQuote = true;
                }
                else{
                    unbalancedSingleQuote = false;
                }
                if((int) c == 0 )
                    break;
                token[i++] = c;
            }
            token[i] = c;

            if(c == '\''){
                unbalancedSingleQuote = false;
            }

            if(unbalancedSingleQuote)
                return new Token(token, Token.Type.BAD_CHAR);

            return new Token(token, Token.Type.CHAR_CONSTANT);
        }
        return null;
    }

    /**
     * Determines if the next token is a block comment. If so, the lexeme will only be '/*'
     * @param token the token char array which that will contain the token.
     * @return a block comment token, or null if not block comment
     */
    private Token checkBlockComment(char[] token){
        char c = getChar();
        if(token[0] == '/' && c == '*') {
            token[1] = '*';
            char first = getChar();
            char second = getChar();
            while (first != '*' && second != '/') {
                if((int) first == 0){
                    return new Token(token, Token.Type.BAD_COMMENT);
                }
                first = second;
                second = getChar();
            }
            return new Token(token, Token.Type.BLOCK_COMMENT);
        }
        ungetChar(c);
        return null;
    }

    /**
     * Determines if the next token is a line comment. If so, the lexeme will only be '//'
     * @param token the token char array which that will contain the token
     * @return a line comment token, or null if not block comment
     */
    private Token checkLineComment(char[] token){
        char c = getChar();
        if(token[0] == '/' && c =='/'){
            token[1] = '/';
            while((c = getChar()) != '\n' && c != 0){
                //Nothing to do, parse until we hit the next line.
            }
            return new Token(token, Token.Type.LINE_COMMENT);
        }
        ungetChar(c);
        return null;
    }

    /**
     * Determines if the next token is an identifier.
     * @param token the token char array which that will contain the token
     * @return an identifier token, or null if not identifier
     */
    private Token checkIdentifier(char[] token){
        char c = token[0];
        int i = 1;
        if(Character.isAlphabetic(c) || c == '_' || c == '$'){
            c = getChar();
            while(Character.isLetterOrDigit(c) || c == '_' || c == '$'){
                token[i++] = c;
                c = getChar();
            }
            ungetChar(c);

            return new Token(token, Token.Type.IDENTIFIER);
        }
        return null;
    }

    /**
     * Determines if the next token is a keyword.
     * @param token the token char array which that will contain the token
     * @return a keyword token, or null if not identifier
     */
    private Token checkKeyword(char[] token){
        int i = 1;
        if(Character.isWhitespace(token[0])){
            token[0] = 0;
            i = 0;
        }
        char[] potentialKw = new char[token.length];
        potentialKw[0] = token[0];

        char c = getChar();

        while(!Character.isWhitespace(c) && c != 0){
            potentialKw[i++] = c;
            c = getChar();
        }
        if(c == ' ')
            ungetChar(c);

        Token.Type type = this.keywordTypes.get(new String(potentialKw, 0, i));
        if(type != null){
            return new Token(potentialKw, type);
        }
        else{
            //> and not >= because we already have the first character in the token[].
            //when the next function is called, the first character should be the one
            //looked at in the token[], and then the stack.
            for(int j = i - 1; j > 0; j--){
                ungetChar(potentialKw[j]);
            }
        }
        return null;
    }

    /**
     * Determines if the next token is a punctuator.
     * @param token the token char array which that will contain the token
     * @return a punctuator token, or null if not identifier
     */
    private Token checkPunctuators(char[] token){
        char c = token[0];
        char first;
        char second;
        switch(c){
            case '[':
                return new Token(token, Token.Type.OPEN_BRACKET);
            case ']':
                return new Token(token, Token.Type.CLOSE_BRACKET);
            case '(':
                return new Token(token, Token.Type.OPEN_PARENT);
            case ')':
                return new Token(token, Token.Type.CLOSE_PARENT);
            case '{':
                return new Token(token, Token.Type.OPEN_BRACE);
            case '}':
                return new Token(token, Token.Type.CLOSE_BRACE);
            case '.': //case covered in numbers method. Gets periods and variadics. checks for .. and returns bad token.
                first = getChar();
                if(first == '.'){
                    token[1] = first;
                    return new Token(token, Token.Type.BAD_PUNCTUATOR);
                }
                else{
                    ungetChar(first);
                    return null;
                }
            case '-':
                first = getChar();
                if(first == '>') {
                    token[1] = first;
                    return new Token(token, Token.Type.ARROW);
                }
                else if(first == '-'){
                    token[1] = first;
                    return new Token(token, Token.Type.DECREMENT);
                }
                else if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.MINUS_EQUALS);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.SUB_OP);
                }
            case '+':
                first = getChar();
                if(first == '+'){
                    token[1] = first;
                    return new Token(token, Token.Type.INCREMENT);
                }
                else if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.PLUS_EQUALS);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.ADD_OP);
                }
            case '*':
                first = getChar();
                if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.MULT_EQUALS);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.MULT_OP);
                }
            case '/':
                first = getChar();
                if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.DIV_EQUALS);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.DIV_OP);
                }
            case '%':
                first = getChar();
                second = getChar();
                char third = getChar();
                if(first == ':' && second == '%' && third == ':'){
                    token[1] = first;
                    token[2] = second;
                    token[3] = third;
                    return new Token(token, Token.Type.PERCENT_COLON_PERCENT_COLON);
                }
                else if(first == '='){
                    token[1] = first;
                    ungetChar(third);
                    ungetChar(second);
                    return new Token(token, Token.Type.MOD_EQUALS);
                }
                else if(first == ':'){
                    token[1] = first;
                    ungetChar(third);
                    ungetChar(second);
                    return new Token(token, Token.Type.PERCENT_COLON);
                }
                else if(first == '>'){
                    token[1] = first;
                    return new Token(token, Token.Type.RIGHT_ANGLE_PERCENT);
                }
                else{
                    ungetChar(third);
                    ungetChar(second);
                    ungetChar(first);
                    return new Token(token, Token.Type.MOD_OP);
                }
            case '&':
                first = getChar();
                if(first == '&'){
                    token[1] = first;
                    return new Token(token, Token.Type.LOG_AND);
                }
                else if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.ASSIGN_AND);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.BIT_AND);
                }
            case '~':
                return new Token(token, Token.Type.BIT_NOT);
            case '!':
                first = getChar();
                if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.NOT_EQUAL);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.LOG_NOT);
                }
            case '^':
                first = getChar();
                if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.ASSIGN_XOR);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.BIT_XOR);
                }
            case '|':
                first = getChar();
                if(first == '|'){
                    token[1] = first;
                    return new Token(token, Token.Type.LOG_OR);
                }
                else if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.ASSIGN_OR);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.BIT_OR);
                }
            case '<':
                first = getChar();
                second = getChar();
                if(first == '<' && second == '='){
                    token[1] = first;
                    token[2] = second;
                    return new Token(token, Token.Type.ASSIGN_LEFT);
                }
                else if(first == '<'){
                    token[1] = first;
                    ungetChar(second);
                    return new Token(token, Token.Type.BIT_LEFT);
                }
                else if(first == '='){
                    token[1] = first;
                    ungetChar(second);
                    return new Token(token, Token.Type.LESS_THAN_EQUAL);
                }
                else if(first == ':'){
                    token[1] = first;
                    ungetChar(second);
                    return new Token(token, Token.Type.LEFT_ANGLE_COLON);
                }
                else if(first == '%'){
                    token[1] = first;
                    ungetChar(second);
                    return new Token(token, Token.Type.LEFT_ANGLE_PERCENT);
                }
                else{
                    ungetChar(second);
                    ungetChar(first);
                    return new Token(token, Token.Type.LESS_THAN);
                }
            case '>':
                first = getChar();
                second = getChar();
                if(first == '>' && second == '='){
                    token[1] = first;
                    token[2] = second;
                    return new Token(token, Token.Type.ASSIGN_RIGHT);
                }
                else if(first == '>'){
                    token[1] = first;
                    ungetChar(second);
                    return new Token(token, Token.Type.BIT_RIGHT);
                }
                else if(first == '='){
                    token[1] = first;
                    ungetChar(second);
                    return new Token(token, Token.Type.GREATER_THAN_EQUAL);
                }
                else{
                    ungetChar(second);
                    ungetChar(first);
                    return new Token(token, Token.Type.GREATER_THAN);
                }
            case '=':
                first = getChar();
                if(first == '='){
                    token[1] = first;
                    return new Token(token, Token.Type.EQUAL);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.ASSIGN);
                }
            case '?':
                return new Token(token, Token.Type.TERNARY);
            case ':':
                first = getChar();
                if(first == '>'){
                    token[1] = first;
                    return new Token(token, Token.Type.RIGHT_ANGLE_COLON);
                }
                else{
                    ungetChar(first);
                    return new Token(token, Token.Type.COLON);
                }
            case ';':
                return new Token(token, Token.Type.SEMI_COLON);
            case ',':
                return new Token(token, Token.Type.COMMA);
            case '#':
                return new Token(token, Token.Type.POUND);
            default:
                return null;
        }
    }

    /**
     * Gets the next char from the input stream, or the top char on the stack if we saved any.
     * @return the next char from the input stream, or the top char on the stack if we saved any.
     */
    private char getChar(){
        try {
            if(this.stack.isEmpty()){
                int code = this.br.read();
                if(code != -1){
                    return (char) code;
                }
            }
            else{
                return this.stack.pop();
            }
        } catch (IOException e) {

        }
        return 0;
    }

    /**
     * Ungets a character from the input stream by storing in stack. If getChar is called, char from stack
     * will be popped before the next character is read from input stream.
     * @param c the char to save
     */
    private void ungetChar(char c){
        this.stack.push(c);
    }

    /**
     * Reads through white spaces on input stream to get to next token.
     */
    private void skipWhiteSpaces(){
        boolean whiteSpace = true;
        char ch = getChar();
        int c = (int) ch;
        while(whiteSpace && c > 0){
            if(Character.isWhitespace(c) || c == '\r'){
                c = getChar();
            } else {
                whiteSpace = false;
                ungetChar((char) c);
            }
        }
    }

    /**
     * Creates the map for keywords to the their respective Tokens.
     */
    private void initKeywordMap(){
        this.keywordTypes = new HashMap<>();
        this.keywordTypes.put("auto", Token.Type.AUTO_KEYWORD);
        this.keywordTypes.put("break", Token.Type.BREAK_KEYWORD);
        this.keywordTypes.put("case", Token.Type.CASE_KEYWORD);
        this.keywordTypes.put("char", Token.Type.CHAR_KEYWORD);
        this.keywordTypes.put("const", Token.Type.CONST_KEYWORD);
        this.keywordTypes.put("continue", Token.Type.CONTINUE_KEYWORD);
        this.keywordTypes.put("default", Token.Type.DEFAULT_KEYWORD);
        this.keywordTypes.put("do", Token.Type.DO_KEYWORD);
        this.keywordTypes.put("double", Token.Type.DOUBLE_KEYWORD);
        this.keywordTypes.put("else", Token.Type.ELSE_KEYWORD);
        this.keywordTypes.put("enum", Token.Type.ENUM_KEYWORD);
        this.keywordTypes.put("extern", Token.Type.EXTERN_KEYWORD);
        this.keywordTypes.put("float", Token.Type.FLOAT_KEYWORD);
        this.keywordTypes.put("for", Token.Type.FOR_KEYWORD);
        this.keywordTypes.put("goto", Token.Type.GOTO_KEYWORD);
        this.keywordTypes.put("if", Token.Type.IF_KEYWORD);
        this.keywordTypes.put("inline", Token.Type.INLINE_KEYWORD);
        this.keywordTypes.put("int", Token.Type.INT_KEYWORD);
        this.keywordTypes.put("long", Token.Type.LONG_KEYWORD);
        this.keywordTypes.put("register", Token.Type.REGISTER_KEYWORD);
        this.keywordTypes.put("restrict", Token.Type.REGISTER_KEYWORD);
        this.keywordTypes.put("return", Token.Type.RETURN_KEYWORD);
        this.keywordTypes.put("short", Token.Type.SHORT_KEYWORD);
        this.keywordTypes.put("signed", Token.Type.SIGNED_KEYWORD);
        this.keywordTypes.put("sizeof", Token.Type.SIZEOF_KEYWORD);
        this.keywordTypes.put("static", Token.Type.STATIC_KEYWORD);
        this.keywordTypes.put("struct", Token.Type.STRUCT_KEYWORD);
        this.keywordTypes.put("switch", Token.Type.SWITCH_KEYWORD);
        this.keywordTypes.put("typedef", Token.Type.TYPEDEF_KEYWORD);
        this.keywordTypes.put("union", Token.Type.UNION_KEYWORD);
        this.keywordTypes.put("unsigned", Token.Type.UNSIGNED_KEYWORD);
        this.keywordTypes.put("void", Token.Type.VOID_KEYWORD);
        this.keywordTypes.put("volatile", Token.Type.VOLATILE_KEYWORD);
        this.keywordTypes.put("while", Token.Type.WHILE_KEYWORD);
    }

    /**
     * Creates a set of punctuators for use in program.
     */
    private void initPuncSet(){
        this.punctuators = new HashSet<>();
        this.punctuators.add('[');
        this.punctuators.add(']');
        this.punctuators.add('(');
        this.punctuators.add(')');
        this.punctuators.add('{');
        this.punctuators.add('}');
        this.punctuators.add('-');
        this.punctuators.add('+');
        this.punctuators.add('&');
        this.punctuators.add('*');
        this.punctuators.add('~');
        this.punctuators.add('!');
        this.punctuators.add('|');
        this.punctuators.add('/');
        this.punctuators.add('%');
        this.punctuators.add('<');
        this.punctuators.add('>');
        this.punctuators.add('=');
        this.punctuators.add('^');
        this.punctuators.add('|');
        this.punctuators.add('?');
        this.punctuators.add(':');
        this.punctuators.add(';');
        this.punctuators.add(',');
    }

    /**
     * Creates a set of all alphabetic characters allowed in hex numbers.
     */
    private void initHexSet(){
        this.hexSet = new HashSet<>();
        this.hexSet.add('A');
        this.hexSet.add('B');
        this.hexSet.add('C');
        this.hexSet.add('D');
        this.hexSet.add('E');
        this.hexSet.add('F');
        this.hexSet.add('a');
        this.hexSet.add('b');
        this.hexSet.add('c');
        this.hexSet.add('d');
        this.hexSet.add('e');
        this.hexSet.add('f');
    }

    /**
     * Checks if the character is an integer suffix.
     * @param c the character to be checked
     * @return true if the char is an int suffix, false otherwise.
     */
    private boolean isIntSuffix(char c){
        return c == 'l' || c == 'L' || c == 'u' || c == 'U';
    }

    /**
     * Checks if the character is an floating point suffix.
     * @param c the character to be checked
     * @return true if the char is an floating point suffix, false otherwise.
     */
    private boolean isFloatSuffix(char c){
        return c == 'f' || c == 'F' || c == 'l' || c == 'L';
    }

    /**
     * Takes C file from command line. All tokens are printed on separate line. If no
     * C file is supplied, then a packaged C file will be run to show that program works.
     * @param args The C file to be run
     */
    public static void main(String[] args) {
        String path;
        if(args.length > 0){
            path = args[0];
        }
        else{
            path = "src/CFile.c";
        }
        Tokenizer t = new Tokenizer(path);
        Token tk = t.nextToken();
        while (!tk.getType().equals(Token.Type.END)) {
            System.out.println(tk);
            tk = t.nextToken();
        }
        System.out.println(tk);
    }
}
