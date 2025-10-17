import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.StdOut;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class BaseballElimination {
    private final int numTeams;
    private final Bag<String> teamString;
    private final int[][] games;
    private final int[] win;
    private final int[] loss;
    private final int[] rem;
    private final HashMap<String, Integer> map;
    private final HashMap<Integer, String> allTeam;

    private int[][] gameVertices;
    private int[] teamVertices;
    private int sum;

    private static final double INF = Double.POSITIVE_INFINITY;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        if (filename == null) throw new IllegalArgumentException("filename is null");

        Bag<String> teamsBag = new Bag<>();
        HashMap<String, Integer> nameToId = new HashMap<>();
        HashMap<Integer, String> idToName = new HashMap<>();

        int n;
        int[][] g;
        int[] w, l, r;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            if (line == null) throw new IllegalArgumentException("empty file");
            n = Integer.parseInt(line.trim());

            w = new int[n];
            l = new int[n];
            r = new int[n];
            g = new int[n][n];

            int i = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] t = line.trim().split("\\s+");

                String teamName = t[0];
                idToName.put(i, teamName);
                nameToId.put(teamName, i);
                teamsBag.add(teamName);

                w[i] = Integer.parseInt(t[1]);
                l[i] = Integer.parseInt(t[2]);
                r[i] = Integer.parseInt(t[3]);
                for (int j = 0; j < n; j++) g[i][j] = Integer.parseInt(t[4 + j]);
                i++;
            }
            if (i != n) throw new IllegalArgumentException("team count mismatch");
        }
        catch (IOException | NumberFormatException e) {
            throw new IllegalArgumentException("cannot read file: " + e.getMessage(), e);
        }

        numTeams = n;
        teamString = teamsBag;
        map = nameToId;
        allTeam = idToName;
        games = g;
        win = w;
        loss = l;
        rem = r;

        gameVertices = new int[numTeams][numTeams];
        teamVertices = new int[numTeams];
    }

    private void validateTeam(String team) {
        if (team == null || !map.containsKey(team))
            throw new IllegalArgumentException("invalid team: " + team);
    }

    // number of teams
    public int numberOfTeams() {
        return numTeams;
    }

    // all teams
    public Iterable<String> teams() {
        return teamString;
    }

    // number of wins for given team
    public int wins(String team) {
        validateTeam(team);
        return win[map.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        validateTeam(team);
        return loss[map.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        validateTeam(team);
        return rem[map.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        validateTeam(team1);
        validateTeam(team2);
        if (team1.equals(team2)) throw new IllegalArgumentException("same team");
        return games[map.get(team1)][map.get(team2)];
    }

    private Bag<String> trivialEliminators(String team) {
        validateTeam(team);
        Bag<String> bag = new Bag<>();
        int x = map.get(team);
        int capBase = win[x] + rem[x];
        for (int i = 0; i < numTeams; i++) {
            if (win[i] > capBase) bag.add(allTeam.get(i));
        }
        return bag;
    }

    private FordFulkerson buildAndRunFlow(String team) {
        int n = numTeams;
        int x = map.get(team);

        // remap vertices fresh for this run
        for (int i = 0; i < n; i++) {
            teamVertices[i] = -1;
            for (int j = 0; j < n; j++) gameVertices[i][j] = -1;
        }

        int s = 0, pos = 1;

        // game vertices for pairs not involving x
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i == x || j == x) continue;
                gameVertices[i][j] = pos;
                gameVertices[j][i] = pos;
                pos++;
            }
        }

        // team vertices (exclude x)
        for (int i = 0; i < n; i++) {
            if (i == x) continue;
            teamVertices[i] = pos++;
        }

        int t = pos;                 // sink index
        FlowNetwork G = new FlowNetwork(t + 1);

        // edges: s -> game(i,j), game(i,j) -> team(i/j)
        sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i == x || j == x) continue;
                int gv = gameVertices[i][j];
                if (gv < 0) continue;
                sum += games[i][j];
                G.addEdge(new FlowEdge(s, gv, games[i][j]));
                G.addEdge(new FlowEdge(gv, teamVertices[i], INF));
                G.addEdge(new FlowEdge(gv, teamVertices[j], INF));
            }
        }

        // edges: team(i) -> t with capacity (wins[x] + rem[x] - wins[i])
        int capBase = win[x] + rem[x];
        for (int i = 0; i < n; i++) {
            if (i == x) continue;
            int cap = capBase - win[i];
            if (cap < 0) cap = 0; // trivial handled separately, but safe
            G.addEdge(new FlowEdge(teamVertices[i], t, cap));
        }

        return new FordFulkerson(G, s, t);
    }


    // is given team eliminated?
    public boolean isEliminated(String team) {
        validateTeam(team);

        Bag<String> triv = trivialEliminators(team);
        if (!triv.iterator().hasNext()) {
            FordFulkerson ff = buildAndRunFlow(team);
            return (int) ff.value() != sum;
        }
        else {
            return true;
        }
    }


    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        validateTeam(team);

        Bag<String> triv = trivialEliminators(team);
        if (triv.iterator().hasNext()) return triv; // trivial certificate

        if (!isEliminated(team)) return null;

        FordFulkerson ff = buildAndRunFlow(team); // recompute to have fresh mappings
        Bag<String> subset = new Bag<>();
        int x = map.get(team);
        for (int i = 0; i < numTeams; i++) {
            if (i == x) continue;
            if (ff.inCut(teamVertices[i])) subset.add(allTeam.get(i));
        }
        return subset;
    }


    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
