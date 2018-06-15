package variblePackage;
import scopePackage.Scope;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableFactory {

    private static final String COMMA = ",";
    private static VariableFactory instance = new VariableFactory();

    private static LinkedList<Scope> currentStack;

    public static VariableFactory getInstance() {
        return instance;
    }

    private VariableFactory(){

    }

    public void setCurrentStack(LinkedList<Scope> stack){
        currentStack = stack;
    }

    /**
     * variable creator
     * @param line a code line
     * @return a variable object fits the line
     * @throws VariableException if
     */
    public Variable createVariable(String line) throws VariableException {
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher(line);
        List<String> foundMatches = new ArrayList<>();
        while(matcher.find()){
            foundMatches.add(matcher.group());
        }
        return getVariableFromList(foundMatches);
    }

    /**
     *
     * @param foundMatches is
     * @return item
     * @throws VariableException if
     */
    private Variable getVariableFromList(List<String> foundMatches) throws VariableException {
        boolean isFinal = false;
        int valStart;
        String vType, vName, vValue = null;
        List<String> varsValues;
        if(foundMatches.get(0).equals("final")){
            isFinal = true;
            vType = foundMatches.get(1);
            valStart = 2;
        }
        else {
            vType = foundMatches.get(0);
            valStart = 1;
        }
        varsValues = foundMatches.subList(valStart, foundMatches.size());
        vName = varsValues.get(0);
        if(varsValues.size() == 2){
            vValue = varsValues.get(1);
        }
        return new Variable(vType, vName, vValue, isFinal);
    }

    private Variable[] multiAssignment(String line) throws VariableException {
        boolean isFinal = false;
        if(line.startsWith("final")){
            line = line.subSequence(6, line.length()).toString();
            isFinal = true;
        }
        String[] splitLine = line.split(" ");
        String vType = splitLine[0];
        List<String[]> stringList = splitArrayBy(Arrays.copyOfRange(splitLine,
                1, splitLine.length), ",");
        LinkedList<String> names = new LinkedList<>();
        LinkedList<String> values = new LinkedList<>();
        for(String[] sequence: stringList){
            names.add(sequence[0]);
            if(sequence.length == 3){
                values.add(sequence[2]);
            }
            else{
                values.add(null);
            }
        }
        Variable[] variables = new Variable[names.size()];
        for(int i = 0; i < variables.length; i++){
            variables[i] = new Variable(vType, names.get(i), values.get(i), isFinal);
        }
        return variables;
    }

    private List<String[]> splitArrayBy(String[] array, String s){
        LinkedList<String[]> arrays = new LinkedList<String[]>();
        int start = 0;
        for(int i = 0; i < array.length; i++){
            if(array[i].equals(s) && i < array.length - 1){
                arrays.add(Arrays.copyOfRange(array, start, i));
                start = i + 1;
            }
            else if(i == array.length - 1){
                arrays.add(Arrays.copyOfRange(array, start, i));
            }
        }
        //Removing the ';'
        arrays.get(arrays.size() - 1)[arrays.get(arrays.size() - 1).length - 1] =
                arrays.get(arrays.size() - 1)[arrays.get(arrays.size() - 1).length - 1].split(";")[0];
        return arrays;
    }
    // TODO: in case of reassignment loop running over all scopes in stack,
    // todo - checking for declaration of variable before, if its final, its type and if it exists
    private boolean isLegalReAssignment(Variable variable){
        String name = variable.getName();
        Scope current = currentStack.getFirst();
        while (current != null){
            if (current.getVariables().containsKey(name)){
                Variable originalVariable = current.getVariables().get(name);
                return (originalVariable.getVariableType().equals(variable.getVariableType())
                        && !originalVariable.isFinal());
            }
        }
        return false;
    }

    public static void main(String[] args) {
//        Variable v = createVariable("ab=BDNbbdj  ddsd  dvd0");
        System.out.println("\\d");
    }
}
