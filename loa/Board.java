/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Chen
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
        int index = 0;
        _moves.clear();
        for (Piece[] r : contents) {
            for (Piece p : r) {
                _board[index] = p;
                index++;
            }
        }
        _winner = null;
        _winnerKnown = false;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        } else {
            initialize(INITIAL_PIECES, board.turn());
            for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
                _board[i] = board._board[i];
            }
            _winner = board.winner();
            _winnerKnown = board._winnerKnown;
            _moveLimit = board._moveLimit;
            _lastMove = board._lastMove;
            _lastFrom = board._lastFrom;
            _lastTo = board._lastTo;
            _moves.addAll(board._moves);
        }

    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {

        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. Assumes MOVE.isCapture()
     *  is false. */
    void makeMove(Move move) {
        assert isLegal(move);
        _lastFrom = get(move.getFrom());
        _lastTo = get(move.getTo());
        if (get(move.getTo()) != EMP) {
            _moves.add(move.captureMove());
        } else {
            _moves.add(move);
        }
        set(move.getTo(), get(move.getFrom()));
        set(move.getFrom(), EMP);
        if (_turn == BP) {
            _turn = WP;
        } else {
            _turn = BP;
        }
        _lastMove = move;
        _winnerKnown = false;
        _subsetsInitialized = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move lastMove = _moves.get(_moves.size() - 1);
        if (lastMove.isCapture()) {
            set(lastMove.getFrom(), get(lastMove.getTo()));
            if (get(lastMove.getTo()) == BP) {
                set(lastMove.getTo(), WP);
            } else {
                set(lastMove.getTo(), BP);
            }
        } else {
            set(lastMove.getFrom(), get(lastMove.getTo()));
            set(lastMove.getTo(), EMP);
        }

        if (_turn == BP) {
            _turn = WP;
        } else {
            _turn = BP;
        }
        _winnerKnown = false;
        _subsetsInitialized = false;
        _moves.remove(_moves.size() - 1);
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        if (!from.isValidMove(to)) {
            return false;
        }
        if (get(from) != turn()) {
            return false;
        }
        if (get(from) == EMP) {
            return false;
        }
        if (get(from) == _board[to.index()]) {
            return false;
        }
        if (blocked(from, to)) {
            return false;
        }
        int dir = from.direction(to);
        int distance = from.distance(to);
        int oppDir = (dir + 4) % 8;
        int num = 1;
        int step = 1;
        Square temp = from;
        while (temp != null) {
            temp = from.moveDest(dir, step);
            step += 1;
            if (temp != null && get(temp) != EMP) {
                num += 1;
            }
        }
        temp = from;
        step = 1;
        while (temp != null) {
            temp = from.moveDest(oppDir, step);
            step += 1;
            if (temp != null &&  get(temp) != EMP) {
                num += 1;
            }
        }
        return num == distance;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        ArrayList<Move> legal = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            if (get(ALL_SQUARES[i]) != turn()) {
                continue;
            } else {
                for (int j = 0; j < BOARD_SIZE * BOARD_SIZE; j++) {
                    if (isLegal(ALL_SQUARES[i], ALL_SQUARES[j])) {
                        if (get(ALL_SQUARES[j]) == EMP) {
                            Move temp = Move.mv(ALL_SQUARES[i],
                                    ALL_SQUARES[j], false);
                            legal.add(temp);
                        } else {
                            Move temp = Move.mv(ALL_SQUARES[i],
                                    ALL_SQUARES[j], true);
                            legal.add(temp);
                        }
                    }
                }
            }
        }
        return legal;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        if (!_winnerKnown) {
            computeRegions();
            if (movesMade() >= _moveLimit
                    && _whiteRegionSizes.size() != 1
                    && _blackRegionSizes.size() != 1) {
                _winner = EMP;
                _winnerKnown = true;
            } else if (_whiteRegionSizes.size() == 1) {
                _winner = WP;
                _winnerKnown = true;
            } else if (_blackRegionSizes.size() == 1) {
                _winner = BP;
                _winnerKnown = true;
            } else {
                _winner = null;
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        int dir = from.direction(to);
        int distance = from.distance(to);
        Square temp = from;
        int step = 1;
        while (temp != null) {
            temp = from.moveDest(dir, step);
            if (step < distance
                    &&  get(temp) !=  get(from) &&  get(temp) != EMP) {
                return true;
            }
            step += 1;
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        if (p == EMP) {
            return 0;
        }
        if (get(sq) != p) {
            return 0;
        }
        if (visited[sq.row()][sq.col()]) {
            return 0;
        }
        int num = 1;
        visited[sq.row()][sq.col()] = true;
        for (int i = 0; i < 8; i++) {
            Square temp = sq.moveDest(i, 1);
            if (temp != null) {
                num += numContig(temp, visited, p);
            }
        }
        return num;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (boolean[] r : visited) {
            for (boolean t : r) {
                t = false;
            }
        }
        int tempB = 0;
        int tempW = 0;
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            tempW = numContig(ALL_SQUARES[i], visited, WP);
            tempB = numContig(ALL_SQUARES[i], visited, BP);
            if (tempB != 0) {
                _blackRegionSizes.add(tempB);
            }
            if (tempW != 0) {
                _whiteRegionSizes.add(tempW);
            }

        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;


    /** True iff subsets computation is up-to-date. */
    private Piece _lastTo;

    /** True iff subsets computation is up-to-date. */
    private Piece _lastFrom;

    /** True iff subsets computation is up-to-date. */
    private Move _lastMove;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}
