/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentseswc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gonzalez-l
 */
public class Predicate{

    private String name;
    private ArrayList<String> arguments;

// Construct a subgoal from a String
// of the form predicate(var1,var2,...,varn)
// Inverse function of toString()
    public Predicate(String str){
		this.arguments = new ArrayList<String>();
		Pattern extract = Pattern.compile("\\w+");
		Matcher matcher = extract.matcher(str);
		if (matcher.find()){
			this.name = matcher.group();
			while(matcher.find()){
			this.arguments.add(matcher.group());
			}
		} else {
			this.name = "";
			this.arguments = new ArrayList<String>();
		}
	}

	public Predicate(String pred, ArrayList<String> vars){
    	this.name = pred;
    	this.arguments = vars;

	}

    public Predicate replace (ArrayList<String> args) {

        return new Predicate(this.name, args);
    }
    
    public void replace (String prevArg, String newArg) {
    
        for (int i = 0; i < this.arguments.size(); i++) {
            if (this.arguments.get(i).equals(prevArg)) {
                this.arguments.set(i, newArg);
            }
        }
    }

	public ArrayList<String> getArguments(){
		return this.arguments;
	}

	public Set<String> getArgumentsSet(){
		return new HashSet<String>(this.arguments);
	}

	@Override
	public boolean equals(Object O){
		if (O instanceof Predicate){
			Predicate p = (Predicate) O;
			return (this.name.equals(p.getName())) && (this.arguments.equals(p.getArguments()));

		}else {return false;}

}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 23 * hash + (this.arguments != null ? this.arguments.hashCode() : 0);
		return hash;
	}



@Override
	public String toString(){
		String res = name.concat("(");
		for(String v: arguments){
			res = res.concat(v+",");
		}
		res = (res.substring(0,res.length()-1)).concat(")");
		return res;

	}

	public ArrayList<String> commonVars(Predicate subg){
		ArrayList<String> res = new ArrayList<String>(this.arguments);
		res.retainAll(subg.arguments);
		return res;

	}

	public String getName(){
		return this.name;


	}

	public ArrayList<String> getVars(){
		return this.arguments;
	}

	// This method does not correspond to isVar, just to isArgument
	public boolean isVar(String var){
		return this.arguments.contains(var);
	}

	public Predicate merge(Predicate S, Predicate T){
		ArrayList<String> s = new ArrayList<String>(S.arguments);
		s.addAll(T.arguments);
		return new Predicate(S.name.concat(T.name),s);
	}


}
