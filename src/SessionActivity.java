import java.util.HashMap;
import java.util.Map;


public class SessionActivity {
	private double time;
	public HashMap<String,Integer> questions;
	public HashMap<String,Integer> parsons;
	public HashMap<String,Integer> examples;
	public HashMap<String,Integer> animations;
	public HashMap<String,Integer> lesslets;
	public HashMap<String,Integer> challenges;
	
	public SessionActivity(){
		time =0.0;
		questions = new HashMap<String,Integer>();
		parsons = new HashMap<String,Integer>();
		examples = new HashMap<String,Integer>();
		animations = new HashMap<String,Integer>();
		lesslets = new HashMap<String,Integer>();
		challenges = new HashMap<String,Integer>();
	}
	
	public void addQuestion(String act){
		if(questions.get(act) == null) questions.put(act, 1);
		else questions.put(act,questions.get(act)+1);
	}
	
	public void addParson(String act){
		if(parsons.get(act) == null) parsons.put(act, 1);
		else parsons.put(act,parsons.get(act)+1);
	}
	
	public void addExample(String act){
		if(examples.get(act) == null) examples.put(act, 1);
		else examples.put(act,examples.get(act)+1);
	}
	
	public void addAnimation(String act){
		if(animations.get(act) == null) animations.put(act, 1);
		else animations.put(act,animations.get(act)+1);
	}
	
	public void addLesslet(String act){
		if(lesslets.get(act) == null) lesslets.put(act, 1);
		else lesslets.put(act,lesslets.get(act)+1);
	}
		
	public void addChallenge(String act){
		if(challenges.get(act) == null) challenges.put(act, 1);
		else challenges.put(act,challenges.get(act)+1);
	}
	
	public void addTime(double time){
		this.time += time;
	}
	
	public double getTime(){
		return time;
	}
	
	public int countActivity(){
		int question=0,parson=0,example=0,animation=0,lesslet=0,challenge=0;
		for (Map.Entry<String, Integer> entry : questions.entrySet()) question += entry.getValue();
		for (Map.Entry<String, Integer> entry : parsons.entrySet()) parson += entry.getValue();
		for (Map.Entry<String, Integer> entry : lesslets.entrySet()) lesslet += entry.getValue();
		for (Map.Entry<String, Integer> entry : challenges.entrySet()) challenge += entry.getValue();
		// Examples and Anim Examples are not counted by attempt (each attempt is a line) 
		//for (Map.Entry<String, Integer> entry : examples.entrySet()) e += entry.getValue();
		//for (Map.Entry<String, Integer> entry : animations.entrySet()) a += entry.getValue();
		example = examples.size();
		animation = animations.size();
		return question+parson+example+animation+lesslet+challenge;
	}
	
	public int countSelfAssesment() {
		int question=0,parson=0,lesslet=0,challenge=0;
		
		for (Map.Entry<String, Integer> entry : questions.entrySet()) question += entry.getValue();
		for (Map.Entry<String, Integer> entry : parsons.entrySet()) parson += entry.getValue();
		for (Map.Entry<String, Integer> entry : lesslets.entrySet()) lesslet += entry.getValue();
		for (Map.Entry<String, Integer> entry : challenges.entrySet()) challenge += entry.getValue();
		
		return question+parson+lesslet+challenge;
	}
	
	public int countExampleLines() {
		int example = 0;
		
		for (Map.Entry<String, Integer> entry : examples.entrySet()) example += entry.getValue();
		
		return example;
	}
}
