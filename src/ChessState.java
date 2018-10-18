import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
/// Represents the state of a chess game
class ChessState {
	public static final int MAX_PIECE_MOVES = 27;
	public static final int None = 0;
	public static final int Pawn = 1;
	public static final int Rook = 2;
	public static final int Knight = 3;
	public static final int Bishop = 4;
	public static final int Queen = 5;
	public static final int King = 6;
	public static final int PieceMask = 7;
	public static final int WhiteMask = 8;
	public static final int AllMask = 15;

	int[] m_rows;

	public static int alphaBetaPruning(ChessState state, int depth, int alpha, int beta, boolean isMax) {
		if(state == null) {
			throw new IllegalArgumentException("The state is null, unable to run algorithm on invalid state");
		}
		if(depth == 0) {
			// Return the heuristic of the state.
			Random r = new Random();
			return state.heuristic(r);
		}
		if(isMax) {
			int best = Integer.MIN_VALUE;
			ChessMoveIterator it = state.iterator(true); // if true, checks for white piece.
			ChessState.ChessMove m;
			while(it.hasNext()) {
				ChessState childState = new ChessState(state);
				m = it.next();
				childState.move(m.xSource, m.ySource, m.xDest, m.yDest);
				int h = childState.heuristic(new Random());
				best = Math.max(best, alphaBetaPruning(childState, depth-1, alpha, beta, !isMax));
				alpha = Math.max(alpha, best);
				if(alpha >= beta)
					break;
			}
			return best;
		}
		else {
			int best = Integer.MAX_VALUE;
			ChessMoveIterator it = state.iterator(false); // Checks for black piece.
			ChessState.ChessMove m;
			while(it.hasNext()) {
				m = it.next();
				ChessState childState = new ChessState(state);
				childState.move(m.xSource, m.ySource, m.xDest, m.yDest);
				int h = childState.heuristic(new Random());
				best = Math.min(best, alphaBetaPruning(childState, depth-1, alpha, beta, !isMax));
				beta = Math.min(beta, best);
				if(alpha >= beta)
					break;
			}
			return best;
		}
	}
	// public static int[] findBestMove(ChessState state, int depth) {
	// 	// From the current State, get the best piece to move.
	// 	// Return the x cord, y cord, for the place you want to move.
	// 	/* INITIAL CALL */
	// 	int [] bestMove = new int[4]; // {1, 1, 0, 2}
	// 	for(int i = 0; i < bestMove.length; i++) {
	// 		bestMove[i] = -1;
	// 	}
	// 	 // alphaBetaPruning(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true); // Calling from white standpoint.
	// 	return bestMove;
	// }
	public static ChessState.ChessMove findBestMove(ChessState state, int depth, boolean isWhite) {
		int maxMove = Integer.MIN_VALUE;
		ChessMoveIterator it = state.iterator(isWhite);
		ChessMove bestMove = new ChessMove();
		ChessMove testMove = new ChessMove();
		while(it.hasNext()) {
			ChessState temp = new ChessState(state);
			testMove = it.next();
			temp.move(testMove.xSource, testMove.ySource, testMove.xDest, testMove.yDest);
			int testVal = temp.alphaBetaPruning(temp, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isWhite);
			if(testVal > maxMove) {
				bestMove = testMove;
				maxMove = testVal;
			}
		}
		return bestMove;
	}

	ChessState() {
		m_rows = new int[8];
		resetBoard();
	}

	ChessState(ChessState that) {
		m_rows = new int[8];
		for(int i = 0; i < 8; i++)
			this.m_rows[i] = that.m_rows[i];
	}

	int getPiece(int col, int row) {
		return (m_rows[row] >> (4 * col)) & PieceMask;
	}

	boolean isWhite(int col, int row) {
		return (((m_rows[row] >> (4 * col)) & WhiteMask) > 0 ? true : false);
	}

	/// Sets the piece at location (col, row). If piece is None, then it doesn't
	/// matter what the value of white is.
	void setPiece(int col, int row, int piece, boolean white) {
		m_rows[row] &= (~(AllMask << (4 * col)));
		m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
	}

	/// Sets up the board for a new game
	void resetBoard() {
		setPiece(0, 0, Rook, true);
		setPiece(1, 0, Knight, true);
		setPiece(2, 0, Bishop, true);
		setPiece(3, 0, Queen, true);
		setPiece(4, 0, King, true);
		setPiece(5, 0, Bishop, true);
		setPiece(6, 0, Knight, true);
		setPiece(7, 0, Rook, true);
		for(int i = 0; i < 8; i++)
			setPiece(i, 1, Pawn, true);
		for(int j = 2; j < 6; j++) {
			for(int i = 0; i < 8; i++)
				setPiece(i, j, None, false);
		}
		for(int i = 0; i < 8; i++)
			setPiece(i, 6, Pawn, false);
		setPiece(0, 7, Rook, false);
		setPiece(1, 7, Knight, false);
		setPiece(2, 7, Bishop, false);
		setPiece(3, 7, Queen, false);
		setPiece(4, 7, King, false);
		setPiece(5, 7, Bishop, false);
		setPiece(6, 7, Knight, false);
		setPiece(7, 7, Rook, false);
	}

	/// Positive means white is favored. Negative means black is favored.
	int heuristic(Random rand)
	{
		int score = 0;
		for(int y = 0; y < 8; y++)
		{
			for(int x = 0; x < 8; x++)
			{
				int p = getPiece(x, y);
				int value;
				switch(p)
				{
					case None: value = 0; break;
					case Pawn: value = 10; break;
					case Rook: value = 63; break;
					case Knight: value = 31; break;
					case Bishop: value = 36; break;
					case Queen: value = 88; break;
					case King: value = 500; break;
					default: throw new RuntimeException("what?");
				}
				if(isWhite(x, y))
					score += value;
				else
					score -= value;
			}
		}
		return score + rand.nextInt(3) - 1;
	}

	/// Returns an iterator that iterates over all possible moves for the specified color
	ChessMoveIterator iterator(boolean white) {
		return new ChessMoveIterator(this, white);
	}

	/// Returns true iff the parameters represent a valid move
	boolean isValidMove(int xSrc, int ySrc, int xDest, int yDest) {
		ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
		for(int i = 0; i < possible_moves.size(); i += 2) {
			if(possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
				return true;
		}
		return false;
	}

	/// Print a representation of the board to the specified stream
	void printBoard(PrintStream stream)
	{
		stream.println("  A  B  C  D  E  F  G  H");
		stream.print(" +");
		for(int i = 0; i < 8; i++)
			stream.print("--+");
		stream.println();
		for(int j = 7; j >= 0; j--) {
			stream.print(Character.toString((char)(49 + j)));
			stream.print("|");
			for(int i = 0; i < 8; i++) {
				int p = getPiece(i, j);
				if(p != None) {
					if(isWhite(i, j))
						stream.print("w");
					else
						stream.print("b");
				}
				switch(p) {
					case None: stream.print("  "); break;
					case Pawn: stream.print("p"); break;
					case Rook: stream.print("r"); break;
					case Knight: stream.print("n"); break;
					case Bishop: stream.print("b"); break;
					case Queen: stream.print("q"); break;
					case King: stream.print("K"); break;
					default: stream.print("?"); break;
				}
				stream.print("|");
			}
			stream.print(Character.toString((char)(49 + j)));
			stream.print("\n +");
			for(int i = 0; i < 8; i++)
				stream.print("--+");
			stream.println();
		}
		stream.println("  A  B  C  D  E  F  G  H");
	}

	/// Pass in the coordinates of a square with a piece on it
	/// and it will return the places that piece can move to.
	ArrayList<Integer> moves(int col, int row) {
		ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
		int p = getPiece(col, row);
		boolean bWhite = isWhite(col, row);
		int nMoves = 0;
		int i, j;
		switch(p) {
			case Pawn:
				if(bWhite) {
					if(!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
						checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
				}
				else {
					if(!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
						checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
				}
				break;
			case Bishop:
				for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case Knight:
				checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
				checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
				checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
				checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
				break;
			case Rook:
				for(i = inc(col); true; i = inc(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(i = dec(col); true; i = dec(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(j = inc(row); true; j = inc(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(j = dec(row); true; j = dec(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				break;
			case Queen:
				for(i = inc(col); true; i = inc(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(i = dec(col); true; i = dec(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(j = inc(row); true; j = inc(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(j = dec(row); true; j = dec(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case King:
				checkMove(pOutMoves, inc(col), row, bWhite);
				checkMove(pOutMoves, inc(col), inc(row), bWhite);
				checkMove(pOutMoves, col, inc(row), bWhite);
				checkMove(pOutMoves, dec(col), inc(row), bWhite);
				checkMove(pOutMoves, dec(col), row, bWhite);
				checkMove(pOutMoves, dec(col), dec(row), bWhite);
				checkMove(pOutMoves, col, dec(row), bWhite);
				checkMove(pOutMoves, inc(col), dec(row), bWhite);
				break;
			default:
				break;
		}
		return pOutMoves;
	}

	/// Moves the piece from (xSrc, ySrc) to (xDest, yDest). If this move
	/// gets a pawn across the board, it becomes a queen. If this move
	/// takes a king, then it will remove all pieces of the same color as
	/// the king that was taken and return true to indicate that the move
	/// ended the game.
	boolean move(int xSrc, int ySrc, int xDest, int yDest) {
		if(xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8) {
			System.out.println("xSrc: " + xSrc);
			System.out.println("ySrc: " + ySrc);
			throw new RuntimeException("out of range");
		}
		if(xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
			throw new RuntimeException("out of range");
		int target = getPiece(xDest, yDest);
		int p = getPiece(xSrc, ySrc);
		if(p == None)
			throw new RuntimeException("There is no piece in the source location");
		if(target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
			throw new RuntimeException("It is illegal to take your own piece");
		if(p == Pawn && (yDest == 0 || yDest == 7))
			p = Queen; // a pawn that crosses the board becomes a queen
		boolean white = isWhite(xSrc, ySrc);
		setPiece(xDest, yDest, p, white);
		setPiece(xSrc, ySrc, None, true);
		if(target == King) {
			// If you take the opponent's king, remove all of the opponent's pieces. This
			// makes sure that look-ahead strategies don't try to look beyond the end of
			// the game (example: sacrifice a king for a king and some other piece.)
			int x, y;
			for(y = 0; y < 8; y++) {
				for(x = 0; x < 8; x++) {
					if(getPiece(x, y) != None) {
						if(isWhite(x, y) != white) {
							setPiece(x, y, None, true);
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	static int inc(int pos) {
		if(pos < 0 || pos >= 7)
			return -1;
		return pos + 1;
	}

	static int dec(int pos) {
		if(pos < 1)
			return -1;
		return pos -1;
	}

	boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite) {
		if(col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if(p > 0 && isWhite(col, row) == bWhite)
			return true;
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite) {
		if(col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if(bDiagonal) {
			if(p == None || isWhite(col, row) == bWhite)
				return true;
		}
		else {
			if(p > 0)
				return true;
		}
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	/// Represents a possible  move
	static class ChessMove {
		int xSource;
		int ySource;
		int xDest;
		int yDest;
	}

	/// Iterates through all the possible moves for the specified color.
	static class ChessMoveIterator
	{
		int x, y;
		ArrayList<Integer> moves;
		ChessState state;
		boolean white;

		/// Constructs a move iterator
		ChessMoveIterator(ChessState curState, boolean whiteMoves) {
			x = -1;
			y = 0;
			moves = null;
			state = curState;
			white = whiteMoves;
			advance();
		}

		private void advance() {
			if(moves != null && moves.size() >= 2) {
				moves.remove(moves.size() - 1);
				moves.remove(moves.size() - 1);
			}
			while(y < 8 && (moves == null || moves.size() < 2)) {
				if(++x >= 8) {
					x = 0;
					y++;
				}
				if(y < 8) {
					if(state.getPiece(x, y) != ChessState.None && state.isWhite(x, y) == white)
						moves = state.moves(x, y);
					else
						moves = null;
				}
			}
		}

		/// Returns true iff there is another move to visit
		boolean hasNext() {
			return (moves != null && moves.size() >= 2);
		}

		/// Returns the next move
		ChessState.ChessMove next() {
			ChessState.ChessMove m = new ChessState.ChessMove();
			m.xSource = x;
			m.ySource = y;
			m.xDest = moves.get(moves.size() - 2);
			m.yDest = moves.get(moves.size() - 1);
			advance();
			return m;
		}
	}
	public boolean whiteWins() {
		for(int j = 7; j >= 0; j--) {
			for(int i = 0; i < 8; i++) {
				int p = this.getPiece(i, j);
				if(p != None) {
					if(!this.isWhite(i, j) && p == King) {
						return false;
					}
				}
			}
		}
		return true;
	}
	public boolean blackWins() {
		for(int j = 7; j >= 0; j--) {
			for(int i = 0; i < 8; i++) {
				int p = this.getPiece(i, j);
				if(p != None) {
					if(this.isWhite(i, j) && p == King)
						return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
        // Able to accept arguments.
        int depthFirstAI = Integer.parseInt(args[0]);
        int depthSecondAI= Integer.parseInt(args[1]);
        if(depthFirstAI < 0 || depthSecondAI < 0) {
            throw new RuntimeException("Depth should not be negative");
		}
        if(depthFirstAI == 0) {
            System.out.println("Human Player");
		}
        else {
            System.out.println("AI at depth: " + depthFirstAI);
		}
        if(depthSecondAI == 0) {
            System.out.println("Human Player");
		}
        else {
            System.out.println("AI at depth: " + depthSecondAI);
		}
		ChessState s = new ChessState();
		s.resetBoard();
		Scanner reader = new Scanner(System.in);
		System.out.println(alphaBetaPruning(s, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, true));
		int counter = 0;
		boolean whiteWins = false;
		boolean blackWins = false;
		while(true) {
			if(counter % 2 == 0) {
				// First player's turn
				s.printBoard(System.out);
				System.out.println();
				if(depthFirstAI > 0) {
					// Call ABPruning make isMax true, because calling from white standpoint.
					// int[] moves = findBestMove(s, depthFirstAI);
					// s.move(moves[0], moves[1], moves[2], moves[3]);
				}
				else if(depthFirstAI == 0) {
					System.out.println("Please input the piece location, and the location you want to move it to (White Player)");
					String str = reader.nextLine();
					int colSrc;
					if(str.equals("q") || str.equals("Q"))
						System.exit(0);
					if(str.charAt(0) == 'A' || str.charAt(0) == 'a')
						colSrc = 0;
					else if(str.charAt(0) == 'B' || str.charAt(0) == 'b')
						colSrc = 1;
					else if(str.charAt(0) == 'C' || str.charAt(0) == 'c')
						colSrc = 2;
					else if(str.charAt(0) == 'D' || str.charAt(0) == 'd')
						colSrc = 3;
					else if(str.charAt(0) == 'E' || str.charAt(0) == 'e')
						colSrc = 4;
					else if(str.charAt(0) == 'F' || str.charAt(0) == 'f')
						colSrc = 5;
					else if(str.charAt(0) == 'G' || str.charAt(0) == 'g')
						colSrc = 6;
					else if(str.charAt(0) == 'H' || str.charAt(0) == 'h')
						colSrc = 7;
					else
						colSrc = 0;
					int rowSrc = Integer.parseInt(String.valueOf(str.charAt(1))) - 1;
					int colDest;
					if(str.charAt(2) == 'A' || str.charAt(2) == 'a')
						colDest = 0;
					else if(str.charAt(2) == 'B' || str.charAt(2) == 'b')
						colDest = 1;
					else if(str.charAt(2) == 'C' || str.charAt(2) == 'c')
						colDest = 2;
					else if(str.charAt(2) == 'D' || str.charAt(2) == 'd')
						colDest = 3;
					else if(str.charAt(2) == 'E' || str.charAt(2) == 'e')
						colDest = 4;
					else if(str.charAt(2) == 'F' || str.charAt(2) == 'f')
						colDest = 5;
					else if(str.charAt(2) == 'G' || str.charAt(2) == 'g')
						colDest = 6;
					else if(str.charAt(2) == 'H' || str.charAt(2) == 'h')
						colDest = 7;
					else
						colDest = 0;
					int rowDest = Integer.parseInt(String.valueOf(str.charAt(3))) - 1;
					s.move(colSrc, rowSrc, colDest, rowDest);
				}
				if(s.whiteWins()) {
					System.out.println("White wins");
					break;
				}
			}
			else {
				s.printBoard(System.out);
				System.out.println();
				if(depthSecondAI > 0) {
					// int[] moves = findBestMove(s, depthSecondAI);
					// s.move(moves[0], moves[1], moves[2], moves[3]);
					// Call ABPruning, calling from black piece standpoint.
				}
				else if(depthSecondAI == 0) {
					System.out.println("Please input the piece location, and the location you want to move it to (Black Player)");
					String str = reader.nextLine();
					int colSrc;
					if(str.equals("q") || str.equals("Q"))
						System.exit(0);
					if(str.charAt(0) == 'A' || str.charAt(0) == 'a')
						colSrc = 0;
					else if(str.charAt(0) == 'B' || str.charAt(0) == 'b')
						colSrc = 1;
					else if(str.charAt(0) == 'C' || str.charAt(0) == 'c')
						colSrc = 2;
					else if(str.charAt(0) == 'D' || str.charAt(0) == 'd')
						colSrc = 3;
					else if(str.charAt(0) == 'E' || str.charAt(0) == 'e')
						colSrc = 4;
					else if(str.charAt(0) == 'F' || str.charAt(0) == 'f')
						colSrc = 5;
					else if(str.charAt(0) == 'G' || str.charAt(0) == 'g')
						colSrc = 6;
					else if(str.charAt(0) == 'H' || str.charAt(0) == 'h')
						colSrc = 7;
					else
						colSrc = 0;
					int rowSrc = Integer.parseInt(String.valueOf(str.charAt(1))) - 1;
					int colDest;
					if(str.charAt(2) == 'A' || str.charAt(2) == 'a')
						colDest = 0;
					else if(str.charAt(2) == 'B' || str.charAt(2) == 'b')
						colDest = 1;
					else if(str.charAt(2) == 'C' || str.charAt(2) == 'c')
						colDest = 2;
					else if(str.charAt(2) == 'D' || str.charAt(2) == 'd')
						colDest = 3;
					else if(str.charAt(2) == 'E' || str.charAt(2) == 'e')
						colDest = 4;
					else if(str.charAt(2) == 'F' || str.charAt(2) == 'f')
						colDest = 5;
					else if(str.charAt(2) == 'G' || str.charAt(2) == 'g')
						colDest = 6;
					else if(str.charAt(2) == 'H' || str.charAt(2) == 'h')
						colDest = 7;
					else
						colDest = 0;
					int rowDest = Integer.parseInt(String.valueOf(str.charAt(3))) - 1;
					s.move(colSrc, rowSrc, colDest, rowDest);
				}
				if(s.blackWins()) {
					System.out.println("Black wins");
					break;
				}
			}
			counter++;
		}
	}
}
