package oop.ex6.fileProcessor;


import oop.ex6.fileProcessor.scopePackage.ScopeException;
import oop.ex6.fileProcessor.variblePackage.VariableException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a enum that represent a format of a line.
 */
public enum LineType {
    METHOD(Constants.METHOD_REGEX_STR, ScoopPosition.OUTER_SCOPE,true)
            {public boolean processSentence(String line, FileAnalyzer fileAnalyzer,boolean toProcess)
                    throws VariableException {
                if (this.isMatch(line)){
                    if (toProcess) {
                        fileAnalyzer.getMethodFactory().createMethod(line);
                    }
                    return true;
                }
                return false;
            }},
    ASSIGNMENT(Constants.ASSIGNMENT_REGEX_STR, ScoopPosition.BOTH,false){
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer,boolean toProcess)
                throws VariableException, ScopeException {
            if (this.isMatch(line)){
                if (toProcess) {
                    fileAnalyzer.getVariableFactory().makeAssignment(line);
                }
                return true;
            }
            return false;
        }
    },
    IF(String.format(Constants.CONDITION_REGEX_STR, Constants.IF_STATMENT), ScoopPosition.INNER_SCOPE,true){
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer,boolean toProcess) throws ScopeException {
            return coditisionProcessor(line,fileAnalyzer,toProcess);
        }
    },
    WHILE(String.format(Constants.CONDITION_REGEX_STR, Constants.WHILE_STATMENT), ScoopPosition.INNER_SCOPE,true){
        @Override
        public boolean processSentence(String line,FileAnalyzer fileAnalyzer,boolean toProcess) throws ScopeException, VariableException {
                return coditisionProcessor(line,fileAnalyzer,toProcess);
        }
    },
    METHOD_CALL(Constants.METHOD_CALL_REGEX_STR, ScoopPosition.INNER_SCOPE,false){
        @Override
        public boolean processSentence(String line, FileAnalyzer fileAnalyzer,boolean toProcess) throws ScopeException {
            if (this.isMatch(line)){
                if (toProcess) {
                    fileAnalyzer.getMethodFactory().addMethodCall(line);
                }
                return true;
            }
            return false;
        }
    },
    RETURN(Constants.RETURN_REGEX_STR, ScoopPosition.INNER_SCOPE,false){
        @Override
        public boolean processSentence(String line,FileAnalyzer fileAnalyzer,boolean toProcess) {
            if (this.isMatch(line)){
                if (toProcess) {
                    fileAnalyzer.getMethodFactory().methodReturn();
                }
                return true;
            }
            return false;
        }
    },
    REASSIGNMENT(Constants.REASSIGNMENT_REGEX_STR, ScoopPosition.INNER_SCOPE,false){
        @Override
        public boolean processSentence(String line,FileAnalyzer fileAnalyzer,boolean toProcess) throws VariableException {
            if (this.isMatch(line)){
                if (toProcess) {
                    fileAnalyzer.getVariableFactory().reAssignment(line);
                }
                    return true;
            }
            return false;
        }
    },
    BLANK_LINE(Constants.BLANK_LINE_REGEX_STR,ScoopPosition.BOTH,false) {
        @Override
        public boolean processSentence(String line,FileAnalyzer fileAnalyzer,boolean toProcess) {
            return (this.isMatch(line)||line==null);
        }
    },
    CLOSE_SCOPE(Constants.CLOSE_SCOPE_REGEX_STR,ScoopPosition.BOTH,false) {
        @Override
        public boolean processSentence(String line,FileAnalyzer fileAnalyzer,boolean toProcess) throws ScopeException {
            if (this.isMatch(line)){
                if (toProcess) {
                    fileAnalyzer.getFile().endScope();
                }
                return true;
            }
            return false;
        }
    };
    //todo bracket or spaces?
    private final ScoopPosition scopePosition;
    private final Pattern regex;
    private boolean scopeCreater=false;
    LineType(String regex, ScoopPosition scopePosition,boolean scopeCreater){
        this.regex=Pattern.compile(regex);
        this.scopePosition = scopePosition;
        this.scopeCreater=scopeCreater;
    }

    public boolean isScopeCreater() {
        return scopeCreater;
    }

    public boolean scoopAtPosition(boolean isInner) {
        if (isInner){
            return scopePosition.isInner();
        }else {
            return scopePosition.isOuter();
        }
    }

    public abstract boolean processSentence(String line, FileAnalyzer fileAnalyzer,boolean toProcess)
            throws VariableException,ScopeException;

    boolean isMatch(String line){
        Matcher matcher=this.regex.matcher(line);
        return matcher.matches();
    }

    boolean coditisionProcessor(String line, FileAnalyzer fileAnalyzer,boolean toProcess) throws ScopeException {
        if (this.isMatch(line)){
            if (toProcess) {
                fileAnalyzer.getConditionFactory().assignScope(line);
            }
            return true;
        }
        return false;
    }

    private static class Constants {
        private static final String IF_STATMENT = "if";
        private static final String WHILE_STATMENT = "while";
        private static final String RETURN_REGEX_STR = "^[ \\t]*return[ \\t]*\\;[ \\t]*$";
        private static final String METHOD_CALL_REGEX_STR = "^[ \\t]*[\\w]+[ \\t]*\\((?:[ \\t]*(?:(?:\\\".*\\\")" +
                "|\\w+)(?:[ \\t]*(?:\\,[ \\t]*(?:(?:\\\".*\\\")|\\w+)[ \\t]*)*)?)?\\)[ \\t]*\\;[ \\t]*$";
                //"^[ \\t]*[\\w]+[ \\t]*\\([ \\t]*(?:(?:\\\".*\\\")" +
                //"|\\w+)(?:[ \\t]*(?:\\,[ \\t]*(?:(?:\\\".*\\\")|\\w+)[ \\t]*)*)?\\)[ \\t]*\\;[ \\t]*$";
        private static final String CONDITION_REGEX_STR ="^[ \\t]*%s[ \\t]*\\([ \\t]*(?:[-\\w]+(?:\\.?\\w+)?)(?:[ \\t]" +
                "*(?:\\&{2}|\\|{2})[ \\t]*(?:[-\\w]+(?:\\.?\\w+)?))*[ \\t]*\\)[ \\t]*\\{[ \\t]*$";

        private static final String ASSIGNMENT_REGEX_STR ="^[ \\t]*(?:\\bfinal\\b[ \\t]+)?(?:" +
                "(?!\\bfinal\\b)[A-Za-z]){2,}[ \\t]+[\\w]+(?:[ \\t]*(?:\\=[ \\t]*(?:(?!\\=|\\,)" +
                "(\\w+|(\\\"|\\').*\\1)))|(?:\\,[ \\t]*\\w*[ \\t]*\\w+))*[ \\t]*\\;[ \\t]*$";
//                "^[ \\t]*(?:final )?\\b[ \\t]*(?:(?!\\bfinal\\b)" +
//                "[A-Za-z]){2,}[ \\t]+(?:(?!\\bfinal\\b)[\\w])+(?:[ \\t]*\\=[ \\t]*(?:(?:(?!\\=|\\,)[\\-\\.\\w]+)|" +
//                "(?:(?:\\\"|\\').*(?:\\\"|\\'))+)+)?(?:[ \\t]*\\,[ \\t]*(?:(?!\\bfinal\\b)[\\w])+(?:[ \\t]*\\=" +
//                "[ \\t]*(?:(?:(?!\\=|\\,)[\\.\\-\\w]+)|(?:(?:\\\"|\\').*(?:\\\"|\\')))+)?[ \\t]*)*[ \\t]*\\;[ \\t]*$";
        private static final String METHOD_REGEX_STR = "^[ \\t]*(?:\\bvoid\\b){1}[ \\t]+[\\w]+[ \\t]" +
                "*\\([ \\t]*(?:(?:final )?\\b[ \\t]*(?:(?!\\bfinal\\b)[A-Za-z]){2,}[ \\t]+(?:(?!\\" +
                "bfinal\\b)[\\w])+(?:[ \\t]*\\,[ \\t]*(?:final )?\\b[ \\t]*(?:(?!\\bfinal\\b)[A-Za-z])" +
                "{2,}[ \\t]+(?:(?!\\bfinal\\b)[\\w])+)*[ \\t]*)?\\)[ \\t]*\\{[ \\t]*$";
        private static final String REASSIGNMENT_REGEX_STR = "^[ \\t]*\\w+[ \\t]*\\=[ \\t]*" +
                "(?:(?!\\=|\\,)[\\S])+[ \\t]*(?:\\,[ \\t]*\\w+[ \\t]*\\=[ \\t]*(?:(?!\\=|\\,)" +
                "[\\S])+)*[ \\t]*\\;[ \\t]*$";
        private static final String BLANK_LINE_REGEX_STR = "^(?:\\/{2}.*)?[\\s]*$";
        private static final String CLOSE_SCOPE_REGEX_STR = "^[ \\t]*\\}[ \\t]*$";
    }
    private enum ScoopPosition {
        INNER_SCOPE(false,true),
        OUTER_SCOPE(true,false),
        BOTH(true,true);
        private final boolean inOuterScope;
        private final boolean inInnerScope;
        ScoopPosition(boolean isOutter, boolean isInner){
            this.inInnerScope =isInner;
            this.inOuterScope =isOutter;
        }
        boolean isOuter(){
            return inOuterScope;
        }
        boolean isInner(){
            return inInnerScope;
        }

    }

}

