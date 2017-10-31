/**
 * A class representing a token for a programming language.
 *
 * Created by Avi Amon
 */
public class Token {
    private String lexeme;
    private Type type;

    /**
     * Constructs a Token with given properties.
     * @param lexeme the lexeme text
     * @param type the Token type
     */
    public Token(char[] lexeme, Type type){
        this.lexeme = new String(lexeme);
        this.type = type;
    }

    /**
     * Returns the lexeme text of the Token.
     * @return the lexeme text of the Token
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Gets the Token type of the token.
     * @return the Token type of the token.
     */
    public Type getType() {
        return type;
    }

    /**
     * String representation of a Token.
     * @return the representation of a Token
     */
    @Override
    public String toString() {
        return "Token: " + getType() + ", Lexeme: " + getLexeme();
    }

    /**
     * All possible tokens represented as Type enums.
     */
    public enum Type{
        INTEGER_CONSTANT,
        BAD_OCTAL,
        BAD_HEX,
        BAD_SUFFIX_TOKEN,
        FLOAT_CONSTANT,
        CHAR_CONSTANT,
        STRING_CONSTANT,
        AUTO_KEYWORD,
        DOUBLE_KEYWORD,
        INT_KEYWORD,
        STRUCT_KEYWORD,
        CONST_KEYWORD,
        FLOAT_KEYWORD,
        SHORT_KEYWORD,
        UNSIGNED_KEYWORD,
        BREAK_KEYWORD,
        ELSE_KEYWORD,
        LONG_KEYWORD,
        SWITCH_KEYWORD,
        CONTINUE_KEYWORD,
        FOR_KEYWORD,
        SIGNED_KEYWORD,
        VOID_KEYWORD,
        CASE_KEYWORD,
        ENUM_KEYWORD,
        REGISTER_KEYWORD,
        TYPEDEF_KEYWORD,
        DEFAULT_KEYWORD,
        GOTO_KEYWORD,
        SIZEOF_KEYWORD,
        VOLATILE_KEYWORD,
        CHAR_KEYWORD,
        EXTERN_KEYWORD,
        RETURN_KEYWORD,
        UNION_KEYWORD,
        DO_KEYWORD,
        IF_KEYWORD,
        INLINE_KEYWORD,
        STATIC_KEYWORD,
        WHILE_KEYWORD,
        IDENTIFIER,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        OPEN_PARENT,
        CLOSE_PARENT,
        OPEN_BRACE,
        CLOSE_BRACE,
        ADD_OP,
        SUB_OP,
        MULT_OP,
        DIV_OP,
        MOD_OP,
        INCREMENT,
        DECREMENT,
        PERIOD,
        VARIADIC,
        ARROW,
        LOG_NOT,
        BIT_NOT,
        BIT_LEFT,
        BIT_RIGHT,
        LESS_THAN,
        GREATER_THAN,
        LESS_THAN_EQUAL,
        GREATER_THAN_EQUAL,
        EQUAL,
        NOT_EQUAL,
        BIT_AND,
        BIT_XOR,
        BIT_OR,
        LOG_AND,
        LOG_OR,
        ASSIGN,
        PLUS_EQUALS,
        MINUS_EQUALS,
        MULT_EQUALS,
        DIV_EQUALS,
        MOD_EQUALS,
        ASSIGN_LEFT,
        ASSIGN_RIGHT,
        ASSIGN_AND,
        ASSIGN_OR,
        ASSIGN_XOR,
        COMMA,
        COLON,
        SEMI_COLON,
        POUND,
        TERNARY,
        LEFT_ANGLE_COLON,
        RIGHT_ANGLE_COLON,
        LEFT_ANGLE_PERCENT,
        RIGHT_ANGLE_PERCENT,
        PERCENT_COLON,
        PERCENT_COLON_PERCENT_COLON,
        BLOCK_COMMENT,
        LINE_COMMENT,
        BAD_COMMENT,
        BAD_PUNCTUATOR,
        END,
        BAD_FLOAT,
        BAD_STRING,
        BAD_CHAR
    }
}
