package bwmcts.sparcraft;

public class Constants {

		// number of players in the game
	
		static int Num_Players				= 2;
		static int  TILE_SIZE  =32;
		// maximum number of units a player can have
		public static int Max_Units					= 128;

		// max depth the search can ever handle
		static int Max_Search_Depth			= 50;

		// number of directions that units can move
		public static int Num_Directions				= 4;

		// max number of ordered moves in a search depth
		static int Max_Ordered_Moves			= 10;

		// distance moved for a 'move' command
		static int Move_Distance				= 16;

        // add between a move and attack as penalty
		static int Move_Penalty             = 4;

        // add range to units because of bounding boxes
		static int Range_Addition       = 32;

		// maximum number of moves possible for any unit
		public static int Max_Moves					= Max_Units + Num_Directions + 1;
		static boolean   Use_Unit_Bounding			= false;
		static int Pass_Move_Duration			= 20;
		static float  Min_Unit_DPF				= 0.1f;
		static int Starting_Energy		= 50;

		// whether to use transposition table in search
		static boolean   Use_Transposition_Table	= true;
		static int Transposition_Table_Size	= 100000;
		static int Transposition_Table_Scan	= 10;
		static int Num_Hashes					= 2;
        
        // UCT options
		static int Max_UCT_Children           = 10;

		// rng seeding options
		static boolean Seed_Hash_Time				= false;
		static boolean Seed_Player_Random_Time		= true;

		// directions of movement
		public static int Move_Dir[][] = new int[][]{{-1,0}, {1,0}, {0,1}, {0,-1} };
	
}
