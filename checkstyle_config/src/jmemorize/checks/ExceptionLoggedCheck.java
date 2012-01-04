package jmemorize.checks;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.*;

/*
 * All catch clauses must contain at least one of
 *    - throw
 *    - method call whose regexp matches "logger.severe"() 
 *    - method call whose regexp matches "logThrowable"() 
 *    - assert false;  for exceptions that should not happen
 *    - the last line is a fallthrough comment 
 * the second case also includes Main.getLogger.severe( ... )
 * if you really need a non logged exception, then what?
 * 
 * This does not attempt to deal with nested catches, instead
 * treating a nested catch as an error.
 * 
 * The sections of this code that deal with the fallthrough comment are
 * based on the checkstyle FallThrough check.
 */
public class ExceptionLoggedCheck extends Check
{
    private int catchDepth;
    // state info about the first level catch
    private boolean hasThrow;    
    private boolean hasLogCall;
    private boolean hasAssert;
    private int catchLineNo; // catch start line
    private int lastRCurly; // catch end line

    /** Relief pattern to allow fall throught to the next case branch. */
    private String mReliefPattern = "fallthru|falls? ?through";

    /** Relief regexp. */
    private Pattern mRegExp;

    public int[] getDefaultTokens()
    {
        return new int[]{TokenTypes.LITERAL_CATCH,
            TokenTypes.LITERAL_THROW, 
            TokenTypes.LITERAL_ASSERT, 
            TokenTypes.METHOD_CALL,
            TokenTypes.RCURLY,
        };
    }

    private void clearCatchState() {
        hasThrow = false;
        hasLogCall = false;
        hasAssert = false;
    }

    public void beginTree(DetailAST aRootAST)
    {
        catchDepth = 0;
        mRegExp = Utils.getPattern(mReliefPattern);   
    }

    public void finishTree(DetailAST arg0)
    {
        assert catchDepth == 0;
    }

    public void visitToken(DetailAST ast)
    {
        if (ast.getType() == TokenTypes.LITERAL_CATCH) {
            // found the catch, init the state data and check for the fallthrough
            catchDepth++;
            assert catchDepth > 0;
            if (catchDepth == 1) {
                clearCatchState();
                catchLineNo = ast.getLineNo();
            } else {
                log(ast.getLineNo(), "Dubious coding: Nested Catch clauses");                
            }
        } else if (ast.getType() == TokenTypes.RCURLY && catchDepth == 1) {
            lastRCurly = ast.getLineNo();
        } else if (ast.getType() == TokenTypes.LITERAL_THROW &&
            catchDepth == 1) {
            hasThrow = true;        
        } else if (ast.getType() == TokenTypes.LITERAL_ASSERT &&
            catchDepth == 1) {
            DetailAST childAst = ast.findFirstToken(TokenTypes.EXPR);
            if (childAst != null || childAst.getNumberOfChildren() == 1) {
                DetailAST grandchildAst = childAst.findFirstToken(TokenTypes.LITERAL_FALSE);
                if (grandchildAst != null) {
                    hasAssert = true;                            
                }
            }
        } else if (ast.getType() == TokenTypes.METHOD_CALL &&
            catchDepth == 1) {
            // look for a call whose name ends in logger.severe
            String callText = FullIdent.createFullIdentBelow(ast).getText();
            //log(ast.getLineNo(), "catch method call " + callText);                
            if ( callText.toLowerCase().endsWith("logger.severe") || 
                callText.endsWith("logThrowable") ) { 
                hasLogCall = true;
            }
        } 
    }

    /**
     *   This function borrowed directly from checkstyle:FallThrough
     * Does a regular expression match on the given line and checks that a
     * possible match is within a comment.
     * @param aPattern The regular expression pattern to use.
     * @param aLine The line of test to do the match on.
     * @param aLineNo The line number in the file.
     * @return True if a match was found inside a comment.
     */
    private boolean commentMatch(Pattern aPattern, String aLine, int aLineNo)
    {
        final Matcher matcher = aPattern.matcher(aLine);
        final boolean hit = matcher.find();
        if (hit) {
            final int startMatch = matcher.start();
            // -1 because it returns the char position beyond the match
            final int endMatch = matcher.end() - 1;
            return getFileContents().hasIntersectionWithComment(aLineNo,
                startMatch, aLineNo, endMatch);
        }
        return false;
    }

    public void leaveToken(DetailAST ast)
    {
        if (ast.getType() == TokenTypes.LITERAL_CATCH) {
            //log(catchLineNo, "End of Catch clause found at lines " + catchLineNo + 
            //  "-" + lastRCurly);
            catchDepth--;
            assert catchDepth >= 0;
            if (catchDepth == 0 && !hasThrow && !hasLogCall && !hasAssert) {
                // check for a fall through comment.  Inspired by and based on the FallThrough check
                String[] lines = getLines();
                String line = lines[lastRCurly - 2];
                //log(ast.getLineNo(), "catch clause last line:" + line);                
                if (!commentMatch(mRegExp, line, lastRCurly -1)) {
                    log(catchLineNo, "Catch clause with no throw, log, assert false, or fallthrough.");
                }
            }
        }
    }
}

