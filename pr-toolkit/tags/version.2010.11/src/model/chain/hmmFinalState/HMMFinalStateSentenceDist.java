package model.chain.hmmFinalState;


import model.chain.hmm.HMM;
import model.chain.hmm.HMMCountTable;
import model.chain.hmm.HMMSentenceDist;
import data.WordInstance;


/**
 * Represents a final sentence distribution for an HMM.
 * The particular sentence as sentence of the instance, 
 * we add an extra position to reflect the end 
 * position. Note that there are no parameters for this position.
 * @author javg
 *
 */
public class HMMFinalStateSentenceDist extends HMMSentenceDist{

	public HMMFinalStateSentenceDist(HMM model, WordInstance inst,
			int nrHiddenStates) {
		this.model = model;
		instance = inst;
		//Add last position
		sentenceSize = inst.getNrWords()+1;
		this.nrHiddenStates = nrHiddenStates;
	}
	
	public void initSentenceDist(){
//		System.out.println("Calline init caches");
		makeInitCache();
		makeTransitionCache();
		makeObservationCache();
		//Create Posterior Objects
		//observationPosterior = new double[sentenceSize][nrHiddenStates];
		//No transitions into the final state
		//transitionPosterior=new double[sentenceSize-1][nrHiddenStates][nrHiddenStates];
	}
	
	public double getInitProb(int state) {
		if(state == (nrHiddenStates-1)) return 0;
		return initialCache[state];
	}
	
	/**
	 * position is in range 0..length, 
	 * where length is the length of the sentence (not the sentenceSize variable, 
	 * which is larger than the length of the sentence by 1)
	 */
	public double getTransitionProbability(int position,int prevState, int state) {		
		if(state == nrHiddenStates-1){
			if(position == (sentenceSize-2)){
				if(transitionCache!=null){
					return transitionCache[prevState][state];
				}else{
					return model.transitionProbabilities.getCounts(prevState,state);
				}
			}else{
				return 0;
			}		
		}else{
			if(position == (sentenceSize-2)){
				return 0;
			}else{
				if(transitionCache!=null){
					return transitionCache[prevState][state];
				}else{
					return model.transitionProbabilities.getCounts(prevState,state);
				}
			}
		}
	}

	public double getObservationProbability(int position, int state) {
		if(position == sentenceSize-1){
			return 1;
		} 
		//You cannot observe nothing from the faque state
		if(state == nrHiddenStates - 1){
			return 0;
		}
		if(observationCache != null){
			return observationCache[position][state];
		}else {
			 return model.observationProbabilities.getCounts(state,instance.getWordId(position));
		}
	}
	
	@Override
	public int getWordId(int position) {
		if(position != sentenceSize-1){
			return instance.getWordId(position);
		}else{
			//This should never be called since this position does not exit
			return -1;
		}
	}
	
	public void makeObservationCache(){
		observationCache=new double[sentenceSize][nrHiddenStates];
		for(int wordPos = 0;  wordPos < sentenceSize-1; wordPos++){
			for(int tagID = 0;  tagID < model.getNrRealStates(); tagID++){
				double prob = model.observationProbabilities.getCounts(tagID,instance.getWordId(wordPos));
				observationCache[wordPos][tagID]=prob;
			}
		}
	}
	
	public void updateObservationCounts(HMMCountTable counts) {
		//Last state is dummy state so its ignored
		for (int state = 0; state < getNumberOfHiddenStates()-1; state++) {
			for (int pos = 0; pos < (getNumberOfPositions()-1); pos++) {
				double prob = getStatePosterior(pos, state);
				if(Double.isInfinite(prob) || Double.isNaN(prob)){
					System.out.println("Updating counts for observiation prob not a number");
					prob=0;
				}
				counts.observationCounts.addCounts(state,getWordId(pos), prob);
			}
		}
	}


}
