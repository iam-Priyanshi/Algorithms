import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashSet;

public class BoggleSolver {
    private TrieDict dict = new TrieDict();
    private HashSet<String> set;

    private static class Node {
        private Object value;
        private int depth = 0;
        private Node[] next;

        private Node(int R) {
            next = new Node[R];
        }
    }

    private static class TrieDict {
        private static final int R = 26; // English uppercase letters
        private static final char FST = 'A'; // first letter in the radix
        private Node root = new Node(R);

        private void put(String key) {
            root = put(root, key, 0);
        }

        private Node put(Node x, String key, int d) {
            if (x == null) {
                x = new Node(R);
                x.depth = d;
            }
            if (d == key.length()) {
                x.value = lengthToScore(d);
                return x;
            }
            char c = key.charAt(d);
            x.next[c - FST] = put(x.next[c - FST], key, d + 1);
            return x;
        }

        private int lengthToScore(int len) {
            int score;
            switch (len) {
                case 0:
                case 1:
                case 2:
                    score = 0;
                    break;
                case 3:
                case 4:
                    score = 1;
                    break;
                case 5:
                    score = 2;
                    break;
                case 6:
                    score = 3;
                    break;
                case 7:
                    score = 5;
                    break;
                default:
                    score = 11;
                    break;
            }
            return score;
        }

        private int get(String key) {
            return get(root, key, 0);
        }

        private int get(Node x, String key, int d) {
            if (x == null) return -1;
            if (d == key.length()) {
                if (x.value == null) return 0;
                else return (int) x.value; // cast needed
            }
            char c = key.charAt(d);
            return get(x.next[c - FST], key, d + 1);
        }

        private Node isValidPrefix(Node cached, String prefix) {
            if (cached == null) return isValidPrefix(root, prefix, 0);
            else return isValidPrefix(cached, prefix, cached.depth);
        }

        private Node isValidPrefix(Node x, String prefix, int d) {
            if (x == null) return null;
            if (d == prefix.length()) return x;
            char c = prefix.charAt(d);
            return isValidPrefix(x.next[c - FST], prefix, d + 1);
        }
    }

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        for (String word : dictionary) {
            dict.put(word);
        }
    }

    private Iterable<String> getAllValidWordsRecursive(BoggleBoard board) {
        set = new HashSet<>();
        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                boolean[] marked = new boolean[board.rows() * board.cols()];
                // deal with 'Qu' case
                if (board.getLetter(i, j) == 'Q')
                    dfs(board, i, j, marked, "" + board.getLetter(i, j) + 'U', null);
                else
                    dfs(board, i, j, marked, "" + board.getLetter(i, j), null);
            }
        }
        return set;
    }

    private void dfs(BoggleBoard board, int row, int col,
                     boolean[] marked, String prefix, Node cache) {
        // the current die is searched, mark it!
        marked[row * board.cols() + col] = true;
        // if there are 2 or more chars in the prefix, then check if chars are in the dict.
        if (prefix.length() >= 2) {
            cache = dict.isValidPrefix(cache, prefix);
            if (cache == null) return;
            // cache is not null, then the prefix is valid. When the stored score is gt 0,
            // then the prefix is a valid word.
            if (cache.value != null && (int) cache.value > 0) set.add(prefix);
        }
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (validate(board, i, j) && !marked[i * board.cols() + j]) {
                    /**
                     * Copy the marked array because when trace back the current call level,
                     * then search a different die, the current marked array shall stay unchanged!
                     */
                    boolean[] copiedMarked = new boolean[board.rows() * board.cols()];
                    System.arraycopy(marked, 0, copiedMarked, 0,
                                     board.rows() * board.cols());
                    // deal with 'Qu' case
                    if (board.getLetter(i, j) == 'Q')
                        dfs(board, i, j, copiedMarked,
                            prefix + board.getLetter(i, j) + 'U', cache);
                    else
                        dfs(board, i, j, copiedMarked,
                            prefix + board.getLetter(i, j), cache);
                }
            }
        }
    }

    /**
     * Data structure for dice nodes, cache variables that the system stack will hold
     * when there are recursive calls in the recursive DFS.
     */
    private static class DfsNode {
        private int row;
        private int col;
        private String prefix;
        private boolean[] marked;
        private Node cached;
    }

    private void dfsNonRecursive(BoggleBoard board, int row, int col, Stack<DfsNode> sk) {
        int cols = board.cols();
        DfsNode dfsNode = new DfsNode();
        dfsNode.row = row;
        dfsNode.col = col;
        // deal with 'Qu' case in the board
        if (board.getLetter(row, col) == 'Q')
            dfsNode.prefix = "" + board.getLetter(row, col) + 'U';
        else dfsNode.prefix = "" + board.getLetter(row, col);
        dfsNode.marked = new boolean[board.rows() * board.cols()];
        sk.push(dfsNode);
        // declare some variables for frequent use
        String prefix;
        Node cachedNode;
        while (!sk.isEmpty()) {
            dfsNode = sk.pop();
            // mark that the current die has been searched!
            dfsNode.marked[dfsNode.row * cols + dfsNode.col] = true;
            // if the score in the cached node is gt 0, it's a valid word, store it.
            if (dfsNode.cached != null && dfsNode.cached.value != null
                    && (int) dfsNode.cached.value > 0)
                set.add(dfsNode.prefix);

            for (int i = dfsNode.row - 1; i <= dfsNode.row + 1; i++) {
                for (int j = dfsNode.col - 1; j <= dfsNode.col + 1; j++) {
                    if (validate(board, i, j) && !dfsNode.marked[i * board.cols() + j]) {
                        // deal with 'Qu' case
                        if (board.getLetter(i, j) == 'Q') {
                            prefix = dfsNode.prefix + board.getLetter(i, j) + 'U';
                        }
                        else {
                            prefix = dfsNode.prefix + board.getLetter(i, j);
                        }
                        // check if will-be-searched prefix is valid, when it's invalid,
                        // the candidate node won't be pushed into the stack!!!
                        cachedNode = dict.isValidPrefix(dfsNode.cached, prefix);
                        if (cachedNode != null) {
                            DfsNode validNode = new DfsNode();
                            validNode.row = i;
                            validNode.col = j;
                            validNode.prefix = prefix;
                            // the marked array is the same as is in the recursive DFS method.
                            boolean[] marked = new boolean[board.rows() * board.cols()];
                            System.arraycopy(dfsNode.marked, 0, marked, 0,
                                             board.rows() * board.cols());
                            validNode.marked = marked;
                            validNode.cached = cachedNode;
                            sk.push(validNode);
                        }
                    }
                }
            }
        } // the stack sk is always empty.
    }


    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        set = new HashSet<>();
        Stack<DfsNode> sk = new Stack<>();
        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                dfsNonRecursive(board, i, j, sk);
            }
        }
        return set;
    }

    private boolean validate(BoggleBoard board, int i, int j) {
        if (i < 0 || i >= board.rows()) return false;
        if (j < 0 || j >= board.cols()) return false;
        return true;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word == null) throw new NullPointerException();
        int score = dict.get(word);
        if (score <= 0) return 0;
        else return score;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        int cnt = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
            cnt++;
        }
        StdOut.println("Score = " + score);
        StdOut.println("Entry num: " + cnt);
    }
}
