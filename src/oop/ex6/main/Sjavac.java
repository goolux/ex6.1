package oop.ex6.main;

import oop.ex6.fileProcessor.FileAnalyzer;
import oop.ex6.fileProcessor.NoSuchLineException;
import oop.ex6.fileProcessor.scopePackage.File;
import oop.ex6.fileProcessor.scopePackage.ScopeException;
import oop.ex6.fileProcessor.variblePackage.VariableException;

import java.io.IOException;
import java.util.ArrayList;

public class Sjavac {
    /*-CONSTANT-*/
    private static final int IO_ERROR_MSG = 2;
    private static final int ILLEGAL_CODE = 1;
    private static final int SJAVA_PASSD_CODE = 0;

    public static void main(String[] args) {
        ArrayList<String> sjavaData;

        try {
            sjavaData = Reader.getInstance().readLines(args[0]);
        } catch (IOException e) {
            System.out.print(IO_ERROR_MSG);
            System.err.println(e.getMessage());
            return;
        }

        FileAnalyzer fileAnalyzer = new FileAnalyzer();
        boolean isFirstTime=true;
        for (int i = 0; i <2 ; i++) {
            for (String line : sjavaData) {
                try {
                    fileAnalyzer.anlayzeLine(line,isFirstTime);
                } catch (VariableException | ScopeException | NoSuchLineException e) {
                    System.out.print(ILLEGAL_CODE);
                    System.err.println(e.getMessage());
                    return;
                }
            }
            isFirstTime=false;
            if (!isFirstTime){
                if (fileAnalyzer.getFile().getScopes().size()> File.MIN_SCOPE_SIZE){
                    System.out.print(ILLEGAL_CODE);
                    System.err.println("problem with closing scoops");
                    return;
                }
            }
        }
        try {
            fileAnalyzer.getMethodFactory().cheakMethodCalls();
        } catch (ScopeException e) {
            System.out.print(ILLEGAL_CODE);
            System.err.println(e.getMessage());
            return;
        }
        System.out.println(SJAVA_PASSD_CODE);
    }

}


