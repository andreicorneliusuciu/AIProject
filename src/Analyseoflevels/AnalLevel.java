package Analyseoflevels;

import searchclient.SearchClient;

public class AnalLevel {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		initalize();

	}
	
	public void analGoals(int x,int y){
		char goal = SearchClient.goals[x][y]; 
		int countW = 0;
		int countG = 0;
		System.out.println(goal);
		
		if(SearchClient.walls[x][y-1]){
			countW++;
		}
		if(SearchClient.walls[x][y+1]){
			countW++;
		}
		if(SearchClient.walls[x-1][y]){
			countW++;
		}
		if(SearchClient.walls[x+1][y]){
			countW++;
		}
			
		if(SearchClient.goals[x][y-1]=='a'){
			
		}
		
		System.out.println(countW);
	}

	private static void initalize() {
		String[][] a1 = { { "", "+", "" }, { "", "a", "+" }, { "", "+", "" } };
		String[][] a2 = { { "", "", "" }, { "", "a", "+" }, { "", "+", "" } };
		String[][] a3 = { { "", "+", "" }, { "", "a", "" }, { "", "+", "" } };
		String[][] a4 = { { "", "", "" }, { "", "a", "+" }, { "", "", "" } };
		String[][] a5 = { { "", "b", "" }, { "b", "a", "b" }, { "", "b", "" } };
		String[][] a6 = { { "", "b", "" }, { "", "a", "b" }, { "", "b", "" } };
		String[][] a7 = { { "", "", "" }, { "", "a", "b" }, { "", "b", "" } };
		String[][] a8 = { { "", "b", "" }, { "", "a", "" }, { "", "b", "" } };
		String[][] a9 = { { "", "", "" }, { "", "a", "b" }, { "", "", "" } };
		String[][] a10 = { { "", "", "" }, { "", "a", "" }, { "", "", "" } };
		String[][] a11 = { { "", "+", "" }, { "b", "a", "+" }, { "", "+", "" } };
		String[][] a12 = { { "", "b", "" }, { "b", "a", "+" }, { "", "+", "" } };
		String[][] a13 = { { "", "", "" }, { "b", "a", "+" }, { "", "+", "" } };
		String[][] a14 = { { "", "+", "" }, { "b", "a", "" }, { "", "+", "" } };
		String[][] a15 = { { "", "+", "" }, { "b", "a", "b" }, { "", "+", "" } };
		String[][] a16 = { { "", "", "" }, { "b", "a", "+" }, { "", "", "" } };
		String[][] a17 = { { "", "b", "" }, { "b", "a", "+" }, { "", "", "" } };
		String[][] a18 = { { "", "b", "" }, { "b", "a", "+" }, { "", "b", "" } };

		String[][][] AllCombi = { a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18 };

		for (int a = 0; a < 18; a++) {

			for (int col = 0; col < 3; col++) {

				for (int row = 0; row < 3; row++) {
					if(AllCombi[a][col][row]== ""){
						System.out.print("  ");
					}else
					System.out.print(AllCombi[a][col][row]+" ");
				}
				System.out.println();
			}
			
			System.out.println();
			System.out.println();
		}

	}

}
