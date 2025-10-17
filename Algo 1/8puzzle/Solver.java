import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

public class Solver {

    private SearchNode lastNode;
    private boolean solvable;
    private int minMoves = 0;

    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial) {
        if (initial == null)
            throw new java.lang.IllegalArgumentException();

        int moves = 0;
        int twinMoves = 0;

        Queue<Board> neighbors = new Queue<Board>();
        Queue<Board> twinNeighbors = new Queue<Board>();

        MinPQ<SearchNode> searchNodes = new MinPQ<SearchNode>();
        MinPQ<SearchNode> twinNodes = new MinPQ<SearchNode>();

        SearchNode searchNode = new SearchNode(initial, moves, null);
        SearchNode twinSearchNode = new SearchNode(initial.twin(), twinMoves, null);

        twinNodes.insert(twinSearchNode);
        searchNodes.insert(searchNode);

        boolean solved = false;
        boolean twinSolved = false;

        SearchNode current = null;

        while (!solved && !twinSolved) {
            current = searchNodes.delMin();
            SearchNode predecessor = current.getPredecessor();
            Board temp = current.getBoard();
            solved = temp.isGoal();

            SearchNode twinCurrent = twinNodes.delMin();
            SearchNode twinPredecessor = twinCurrent.getPredecessor();
            Board twinTemp = twinCurrent.getBoard();
            twinSolved = twinTemp.isGoal();

            for (Board b : temp.neighbors())
                neighbors.enqueue(b);

            for (Board b : twinTemp.neighbors())
                twinNeighbors.enqueue(b);

            while (neighbors.size() > 0) {
                Board board = neighbors.dequeue();
                int move = current.getMoves();
                move++;
                if (predecessor != null && predecessor.getBoard().equals(board))
                    continue;

                SearchNode neighborNode = new SearchNode(board, move, current);
                searchNodes.insert(neighborNode);
            }

            while (twinNeighbors.size() > 0) {
                Board board = twinNeighbors.dequeue();
                int twinMove = current.getMoves();
                twinMove++;
                if (twinPredecessor != null && twinPredecessor.getBoard().equals(board))
                    continue;

                SearchNode neighborNode = new SearchNode(board, twinMove, twinCurrent);
                twinNodes.insert(neighborNode);
            }

            moves = current.getMoves() + 1;
            twinMoves = twinCurrent.getMoves() + 1;
            lastNode = current;
        }

        solvable = !twinSolved;
        minMoves = moves - 1;

    }

    // is the initial board solvable? (see below)
    public boolean isSolvable() {
        return solvable;
    }

    // min number of moves to solve initial board; -1 if unsolvable
    public int moves() {
        if (!isSolvable())
            return -1;
        return minMoves;
    }

    // sequence of boards in a shortest solution; null if unsolvable
    public Iterable<Board> solution() {
        Stack<Board> boards = new Stack<Board>();
        SearchNode lastNode = this.lastNode;
        if (this.isSolvable()) {
            while (lastNode.getPredecessor() != null) {
                boards.push(lastNode.getBoard());
                lastNode = lastNode.getPredecessor();
            }
            boards.push(lastNode.getBoard());
            return boards;
        }
        return null;
    }

    private class SearchNode implements Comparable<SearchNode> {

        private SearchNode predecessor = null;
        private Board current = null;
        private int moves = 0;
        private int priority = 0;

        public SearchNode(Board initial, int m, SearchNode pred) {
            predecessor = pred;
            moves = m;
            current = initial;

            priority = m + initial.manhattan();
        }

        public int getPriority() {
            return priority;
        }

        public Board getBoard() {
            Board temp = current;
            return temp;
        }

        public int getMoves() {
            return moves;
        }

        public SearchNode getPredecessor() {
            SearchNode temp = predecessor;
            return temp;
        }

        @Override
        public int compareTo(SearchNode o) {
            return this.priority - o.priority;
        }
    }

    // test client (see below)
    public static void main(String[] args) {

        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tiles[i][j] = in.readInt();
        Board initial = new Board(tiles);

        // solve the puzzle
        Solver solver = new Solver(initial);

        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
