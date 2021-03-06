package oop.ex6.fileProcessor;

/**
 * exception class for case of illegal line
 */
public class NoSuchLineException extends Exception {

        private static final long serialVersionUID = 1L;
        private static final String ILLEGAL_LINE_MSG = "No such file or directory";

    NoSuchLineException() {
        super(ILLEGAL_LINE_MSG);
    }

}

