import java.util.*;

class NFA {

public int Q; // number of states
public int T; // number of transitions
public boolean[][][] trans; // the transitions
public int I[]; // initial states
public int F[]; // final states

// run contains a list the number of consecutive 1's
// for example, runs = { 2 } is equivalent to the
// regular expression 0*110*
public NFA( int[] runs )
{
	Q = 1; // count start state
	for (int i = 0; i < runs.length; ++i)
	{
		Q += runs[i];
		
		// if there's another run ahead, there
		// needs to be at least one 0 between them
		if (i < runs.length - 1)
			Q++;
	}
	trans = new boolean[Q][2][Q];
	
	T = 0;
	int currentState = 0;
	for (int i = 0; i < runs.length; ++i)
	{
		trans[currentState][0][currentState] = true;
		trans[currentState][1][currentState + 1] = true;
		currentState++;
		T += 2;

		for (int j = 1; j < runs[i]; ++j)
		{
			trans[currentState][1][currentState + 1] = true;
			currentState++;
			T++;
		}
		
		// if there's another run ahead, there
		// needs to be at least one 0 between them
		if (i < runs.length - 1)
		{
			trans[currentState][0][currentState + 1] = true;
			currentState++;
			T++;
		}
	}
	
	// add transitions to the final state seperately since since
	// we don't want any transitions on 1s after reaching it
	trans[currentState][0][currentState] = true;
	currentState++;
	T++;
	
	I = new int[1];
	I[0] = 0;
	F = new int[1];
	F[0] = Q - 1;
}

public NFA( Scanner in )
{
	Q = in.nextInt();
	T = in.nextInt();
	int s = in.nextInt();
	I = new int[s];
	for (int i=0; i<s; ++i) I[i] = in.nextInt();
	s = in.nextInt();
	F = new int[s];
	for (int i=0; i<s; ++i) F[i] = in.nextInt();
	trans = new boolean[Q][2][Q];
	for (int i=0; i<T; ++i) 
		trans[in.nextInt()][in.nextInt()][in.nextInt()] = true;
}

public String toString()
{
	String str = new String();
	str += "Q = " + Q + "\r\n";
	str += "T = " + T + "\r\n";
	str += "I = ";
	for (int i = 0; i < I.length; ++i)
	{
		str += I[i] + " ";
	}
	str += "\r\nF = ";
	for (int i = 0; i < F.length; ++i)
	{
		str += F[i] + " ";
	}
	str += "\r\n";
	
	for (int i = 0; i < Q; ++i)
	{
		for (int j = 0; j < 2; ++j)
		{
			for (int k = 0; k < Q; ++k)
			{
				if (trans[i][j][k])
					str += "(" + i + ", " + j + ", " + k +")" + "\r\n";
			}
		}
	}
	
	return str;
}

}

class CNF {

public ArrayList<ArrayList<Integer>> cl; // the clauses as lists of integers
public int C = 0; // count of clauses;
public int V = 0; // count of variables

public CNF()
{
	cl = new ArrayList<ArrayList<Integer>>();
}

// converts an NFA to CNF form using Knuth's algorithm 
// from excercise 436 of section 7.2.2.2 in TAOCP
public CNF ( int n, NFA nfa )
{
	class Transition
	{
		public int transStart;
		public int transChar;
		public int transEnd;
		
		public Transition(int start, int c, int end)
		{
			transStart = start;
			transChar = c;
			transEnd = end;
		}
	}
	ArrayList<Transition> transitions = new ArrayList<Transition>();
	
	for(int transStart = 0; transStart < nfa.Q; ++transStart)
	{
		// iterate over binary alphabet
		for (int a = 0; a <= 1; ++a)
		{
			// iterate over transition beginnings
			for (int transEnd = 0; transEnd < nfa.Q; ++transEnd)
			{
				if (nfa.trans[transStart][a][transEnd])
					transitions.add(new Transition(transStart, a, transEnd));
			}
		}
	}
	
	V = getTransitionVarIdx(n, nfa.Q, n, transitions.size() - 1);
	
	cl = new ArrayList<ArrayList<Integer>>();
	for (int k = 1; k <= n; ++k)
	{
		// clause family (i)
		for (int i = 0; i < transitions.size(); ++i)
		{
			cl.add(new ArrayList<Integer>());
			ArrayList<Integer> clause = cl.get(cl.size() - 1);
			int notTransitionVar = -getTransitionVarIdx(n, nfa.Q, k, i);
			clause.add(notTransitionVar);
			
			if (transitions.get(i).transChar == 1)
				clause.add(k);
			else
				clause.add(-k);
			
			cl.add(new ArrayList<Integer>());
			clause = cl.get(cl.size() - 1);
			clause.add(notTransitionVar);
			clause.add(getStateVarIdx(n, k, transitions.get(i).transEnd));
		}
		
		for (int q = 0; q < nfa.Q; ++q)
		{
			// clause family (ii)
			cl.add(new ArrayList<Integer>());
			ArrayList<Integer> clause = cl.get(cl.size() - 1);
			clause.add(-getStateVarIdx(n, k - 1, q));
			for (int i = 0; i < transitions.size(); ++i)
			{
				if (transitions.get(i).transStart == q)
					clause.add(getTransitionVarIdx(n, nfa.Q, k, i));
			}
			
			// clause family (iii)
			cl.add(new ArrayList<Integer>());
			clause = cl.get(cl.size() - 1);
			clause.add(-getStateVarIdx(n, k, q));
			for (int i = 0; i < transitions.size(); ++i)
			{
				if (transitions.get(i).transEnd == q)
					clause.add(getTransitionVarIdx(n, nfa.Q, k, i));
			}
		}
		
		// clause family (iv)
		for (int a = 0; a <= 1; ++a)
		{
			cl.add(new ArrayList<Integer>());
			ArrayList<Integer> clause = cl.get(cl.size() - 1);
			
			if (a == 1)
				clause.add(-k);
			else
				clause.add(k);
			
			for (int i = 0; i < transitions.size(); ++i)
			{
				if (transitions.get(i).transChar == a)
					clause.add(getTransitionVarIdx(n, nfa.Q, k, i));
			}
		}
		
		// clause family (v)
		for (int i = 0; i < transitions.size(); ++i)
		{
			cl.add(new ArrayList<Integer>());
			ArrayList<Integer> clause = cl.get(cl.size() - 1);
			clause.add(-getTransitionVarIdx(n, nfa.Q, k, i));
			
			Transition t = transitions.get(i);
			for (int q = 0; q < nfa.Q; ++q)
			{
				if (nfa.trans[q][t.transChar][t.transEnd])
					clause.add(getStateVarIdx(n, k - 1, q));
			}
		}
	}
	
	// clause family (vi)
	int[] initialStates = nfa.I.clone();
	int[] finalStates = nfa.F.clone();
	Arrays.sort(initialStates);
	Arrays.sort(finalStates);
	for (int q = 0; q < nfa.Q; ++q)
	{
		if (Arrays.binarySearch(initialStates, q) < 0)
		{
			cl.add(new ArrayList<Integer>());
			cl.get(cl.size() - 1).add(-getStateVarIdx(n, 0, q));
		}
		
		if (Arrays.binarySearch(finalStates, q) < 0)
		{
			cl.add(new ArrayList<Integer>());
			cl.get(cl.size() - 1).add(-getStateVarIdx(n, n, q));
		}
	}
	
	C = cl.size();
}

private static int getStateVarIdx(int n, int k, int stateIndex)
{
	int retIndex = n; // offset for variables x_1 through x_n
	retIndex += (k + 1) + ((n + 1) * stateIndex); // k + 1 and n + 1 because states are 0 indexed
	return retIndex;
}

private static int getTransitionVarIdx(int n, int numStates, int k, int transIdx)
{
	int retIndex = n; // offset for variables x_1 through x_n
	retIndex += numStates * (n + 1); // offset for state variables q_0 through q_n, for each state q
	retIndex += k + (n * transIdx);
	return retIndex;
}

// outputs in DIMACS cnf format
public String toString()
{
	String str = new String();
	str += "p cnf " + V + " " + C + "\r\n";
	for (int i = 0; i < cl.size(); ++i)
	{
		for (int j = 0; j < cl.get(i).size(); ++j)
		{
			str += cl.get(i).get(j) + " ";
		}
		str += "0\r\n";
	}
	
	return str;
}

}