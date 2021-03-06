package oop.ex6.fileProcessor;


import oop.ex6.fileProcessor.scopePackage.ScopeException;
import oop.ex6.fileProcessor.variblePackage.VariableException;
import oop.ex6.fileProcessor.variblePackage.VariableType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a enum that represent a format of a line.
 */
public enum LineType {
    /**a method declaration line*/
    METHOD(RegexConstants.METHOD_REGEX_STR, ScopePosition.OUTER_SCOPE, true) {
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound)
                throws VariableException, ScopeException {
            if (isFound || this.isMatch(line)) {
                if (toProcess) {
                    fileAnalyzer.getMethodFactory().createMethod(line);
                }
                return true;
            }
            return false;
        }
    },

    /**variables assignment line*/
    ASSIGNMENT(RegexConstants.ASSIGNMENT_REGEX_STR, ScopePosition.BOTH, false) {
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound)
                throws VariableException, ScopeException {
            if (isFound || isAssignmentFit(line)) {
                if (toProcess) {
                    fileAnalyzer.getVariableFactory().makeAssignment(line);
                }
                return true;
            }
            return false;
        }

        /**
         * check if line fit to assignment
         * @param line the code line
         * @return true if it fits assignment
         */
        private boolean isAssignmentFit(String line) {
            line = line.trim();
            boolean assignedPrefix = false;
            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1);
                String[] variables = line.split("[ \\t]*+,[ \\t]*+", -1);
                for (String variableLine : variables) {
                    if (!assignedPrefix) {
                        String[] firstVar = variableLine.split("[ \\t]*(?:[ \\t]|=)[ \\t]*+");
                        for (String varType : firstVar) {
                            try {
                                VariableType.parseType(varType);
                                assignedPrefix = true;
                                break;
                            } catch (VariableException ignored) {
                            }
                        }
                        if (!assignedPrefix) {
                            return false;
                        }
                    }
                    if (!this.isMatch(variableLine)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    },

    /**if scope declaration line*/
    IF(String.format(RegexConstants.CONDITION_REGEX_STR, RegexConstants.IF_STATMENT),
            ScopePosition.INNER_SCOPE, true) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) throws ScopeException {
            return conditionProcessor (line, fileAnalyzer, toProcess, isFound);
        }
    },

    /**while scope declaration line*/
    WHILE(String.format(RegexConstants.CONDITION_REGEX_STR, RegexConstants.WHILE_STATMENT),
            ScopePosition.INNER_SCOPE, true) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) throws ScopeException {
            return conditionProcessor (line, fileAnalyzer, toProcess, isFound);
        }
    },

    /**method call line*/
    METHOD_CALL(RegexConstants.METHOD_CALL_REGEX_STR, ScopePosition.INNER_SCOPE, false) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) {
            if (isFound || this.isMatch(line)) {
                if (toProcess) {
                    fileAnalyzer.getMethodFactory().addMethodCall(line);
                }
                return true;
            }
            return false;
        }
    },

    /**return line*/
    RETURN(RegexConstants.RETURN_REGEX_STR, ScopePosition.INNER_SCOPE, false) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) {
            if (isFound || this.isMatch(line)) {
                if (toProcess) {
                    fileAnalyzer.getMethodFactory().methodReturn();
                }
                return true;
            }
            return false;
        }
    },

    /**reassignment line*/
    REASSIGNMENT(RegexConstants.REASSIGNMENT_REGEX_STR, ScopePosition.BOTH, false) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) throws VariableException {
            if (isFound || this.isMatch(line)) {
                if (toProcess) {
                    fileAnalyzer.getVariableFactory().reAssignment(line);
                }
                return true;
            }
            return false;
        }
    },

    /**blank line, or documentation*/
    BLANK_LINE(RegexConstants.BLANK_LINE_REGEX_STR, ScopePosition.BOTH, false) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) {
            return (isFound || this.isMatch(line) || line == null);
        }
    },

    /**close scope line*/
    CLOSE_SCOPE(RegexConstants.CLOSE_SCOPE_REGEX_STR, ScopePosition.BOTH, false) {
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
                                       boolean isFound) throws ScopeException {
            if (isFound || this.isMatch(line)) {
                if (toProcess) {
                    fileAnalyzer.getFile().endScope();
                }
                return true;
            }
            return false;
        }
    };

    /**the line scope position*/
    private final ScopePosition scopePosition;
    /**the regex pattern .*/
    private final Pattern regex;

    /**true if the line create new scope , false otherwise.*/
    private boolean scopeCreater;

    /**
     * LineType constructor.
     * @param regex the propper regex line.
     * @param scopePosition the line scope position.
     * @param scopeCreater true if the line create new scope , false otherwise.
     */
    LineType(String regex, ScopePosition scopePosition, boolean scopeCreater) {
        this.regex = Pattern.compile(regex);
        this.scopePosition = scopePosition;
        this.scopeCreater = scopeCreater;
    }

    /**
     * @return true if the line type was ment to build a new scope,false otherwise.
     */
    public boolean isScopeCreater() {
        return scopeCreater;
    }

    /**
     * @param isInner true for checking if the line type meant to be in inner scope otherwise checking if
     *                the line type meant to be in outer scope
     * @return true if it is , false otherwise
     */
    public boolean scoopAtPosition(boolean isInner) {
        if (isInner) {
            return scopePosition.isInner();
        } else {
            return scopePosition.isOuter();
        }
    }

    /**
     * abstract method, processes line
     * @param line line to process
     * @param fileAnalyzer the analyzer pbject
     * @param toProcess if need to process line
     * @param isFound if is found
     * @return true if it legal
     * @throws VariableException in case problem with variable
     * @throws ScopeException in cae scope not okay
     */
    public abstract boolean processSentence(String line, FileAnalyzer fileAnalyzer, boolean toProcess,
            boolean isFound) throws VariableException, ScopeException;

    /**
     *check if linr match regex
     * @param line code line
     * @return true if it fit
     */
    boolean isMatch(String line) {
        Matcher matcher = this.regex.matcher(line);
        return matcher.matches();
    }

    /**
     * process condition declare
     * @param line the line declare condition
     * @param fileAnalyzer object fileanalyzer
     * @param toProcess if need to process
     * @param isFound if found
     * @return true if condition legal
     * @throws ScopeException if scope illegal
     */
    boolean conditionProcessor(String line, FileAnalyzer fileAnalyzer, boolean toProcess, boolean isFound)
            throws ScopeException {
        if (isFound || this.isMatch(line)) {
            if (toProcess) {
                fileAnalyzer.getConditionFactory().assignScope(line);
            }
            return true;
        }
        return false;
    }

    /**
     * a class which hold a LineType regex Expressions.
     */
    private static class RegexConstants {
        private static final String EXCLUDE_FINEL_IN_VAR_NAME_REGEX = "(?:(?!\\bfinal\\b)[A-Za-z])";
        private static final String CLOSE_COMMA_IN_LINE = "[ \\t]*+\\;[ \\t]*+$";

        private static final String IF_STATMENT = "if";
        private static final String WHILE_STATMENT = "while";
        private static final String RETURN_REGEX_STR = "^[ \\t]*+return" + CLOSE_COMMA_IN_LINE;
        private static final String METHOD_CALL_REGEX_STR = "^[ \\t]*+[\\w]++[ \\t]*\\((?:[ \\t]*" +
                "(?:(?:\\\".*\\\")" +
                "|\\w++)(?:[ \\t]*(?:\\,[ \\t]*(?:(?:\\\".*\\\")|\\w++)[ \\t]*)*+)?)?\\)" +
                CLOSE_COMMA_IN_LINE;
        private static final String CONDITION_REGEX_STR = "^[ \\t]*+%s[ \\t]*+\\([ \\t]*+(?:[-\\w]++" +
                "(?:\\.?\\w++)?)(?:[ \\t]" +
                "*+(?:\\&{2}|\\|{2})[ \\t]*+(?:[-\\w]++(?:\\.?\\w++)?))*+[ \\t]*+\\)[ \\t]*+\\{[ \\t]*+$";

        private static final String ASSIGNMENT_REGEX_STR = "^[ \\t]*+(?:\\bfinal\\b)?[ \\t]*+" +
                "(?:(?!\\breturn\\b)[\\w]++" +
                "[ \\t]++)?(?!\\breturn\\b)[\\w]++(?:[ \\t]*+\\=[ \\t]*+(?:(?:[\\-\\.\\w]++)|(?:(\\'|\\\")" +
                ".*\\1)))?[ \\t]*+$";
        private static final String METHOD_REGEX_STR = "^[ \\t]*+(?:\\bvoid\\b){1}[ \\t]++[\\w]++[ \\t]" +
                "*+\\([ \\t]*+(?:(?:final )?\\b[ \\t]*+" + EXCLUDE_FINEL_IN_VAR_NAME_REGEX +
                "{2,}+[ \\t]++(?:(?!\\" +
                "bfinal\\b)[\\w])++(?:[ \\t]*+\\,[ \\t]*+(?:final )?\\b[ \\t]*+" +
                EXCLUDE_FINEL_IN_VAR_NAME_REGEX +
                "{2,}+[ \\t]++(?:(?!\\bfinal\\b)[\\w])++)*+[ \\t]*+)?\\)[ \\t]*+\\{[ \\t]*+$";
        private static final String REASSIGNMENT_REGEX_STR = "^[ \\t]*+\\w++[ \\t]*+\\=[ \\t]*+" +
                "(?:(?!\\=|\\,)[\\S])+[ \\t]*+(?:\\,[ \\t]*+\\w++[ \\t]*+\\=[ \\t]*+(?:(?!\\=|\\,)" +
                "[\\S])++)*+" + CLOSE_COMMA_IN_LINE;
        private static final String BLANK_LINE_REGEX_STR = "^(?:\\/{2}.*+)?[\\s]*+$";
        private static final String CLOSE_SCOPE_REGEX_STR = "^[ \\t]*+\\}[ \\t]*+$";
    }

    /**
     *inner enum that represent the current scope position;
     */
    private enum ScopePosition {
        /**
         * an only inner scope
         */
        INNER_SCOPE(false, true),
        /**
         * an only outer scope.
         */
        OUTER_SCOPE(true, false),
        /**
         * inner and outer scope together.
         */
        BOTH(true, true);
        /**
         *if the line type should be in outer scope.
         */
        private final boolean inOuterScope;
        /**
         * if the line type should be in inner scope.
         */
        private final boolean inInnerScope;

        /**
         * enum constructor
         * @param isOuter if the line type should be in outer scope.
         * @param isInner if the line type should be in inner scope.
         */
        ScopePosition(boolean isOuter, boolean isInner) {
            this.inInnerScope = isInner;
            this.inOuterScope = isOuter;
        }

        /**
         * @return boolean answer if it is an outer scope line
         */
        boolean isOuter() {
            return inOuterScope;
        }

        /**
         * @return boolean answer if it is an inner scope line
         */
        boolean isInner() {
            return inInnerScope;
        }

    }

}

