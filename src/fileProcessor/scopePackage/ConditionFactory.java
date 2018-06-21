package fileProcessor.scopePackage;

import fileProcessor.variblePackage.Variable;
import fileProcessor.variblePackage.VariableType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * this we call when we get a condition line.
 * it checks everything is fine with the condition, then sticks the condition in the head of the stack.
 */
public class ConditionFactory {

    private File file;

     public ConditionFactory(File file){
        this.file=file;
     }

    /**
     * gets variables from parentouses.
     * @param line
     * @return
     */
    private List<String> getVariables(String line){
        String l = line.split("\\{")[0];
        l = l.split("\\(")[1];
        l = l.split("\\)")[0];
        String[] vars = l.split("\\||");
        List<String> variables = new LinkedList<>();
        for(String s: vars){
            for(String str: s.split("&&"))
            variables.add(s.replaceAll("\\s+", ""));
        }
        return variables;
    }

    /**
     * checks if variables are legal to assign
     * @param variables
     * @return
     */
    private boolean areVariablesLegit(List<String> variables){
        LinkedList<Scope> scopes = this.file.getScopes();
        HashMap<String, Variable> fatherVars;
        for(String variable: variables){
            if(variable.equals("true")||variable.equals("false")) { continue; }
            try{
                Double.parseDouble(variable);
            }
            catch (NumberFormatException e) {
                for (Scope scope: scopes) {
                    fatherVars = scope.getVariables();
                    if (fatherVars.containsKey(variable)) {
                        if (!isValueBoolean(fatherVars.get(variable))) {
                            return false;
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean isValueBoolean(Variable variable){
        VariableType[] legalTypes = {VariableType.INTEGER, VariableType.BOOLEAN, VariableType.BOOLEAN};
        for(VariableType type: legalTypes){
            if(variable.getVariableType().equals(type)){
                return true;
            }
        }
        return false;
    }

    public void assignScope(String line) throws ScopeException {
        if(areVariablesLegit(getVariables(line))){
            HashMap<String, Variable> variables = new HashMap<>();
            ConditionScope conditionScope = new ConditionScope(variables, file.getCurrentScope());
            file.addScope(conditionScope);
        }
        else {
            throw new ScopeException("illegal if/while line");
        }
    }

}