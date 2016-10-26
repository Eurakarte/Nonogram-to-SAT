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