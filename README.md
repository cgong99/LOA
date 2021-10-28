# Lines of Action

Berkely CS61B project: https://inst.eecs.berkeley.edu/~cs61b/sp20/materials/proj/proj2/index.html

### Introduction

*Lines of Action* is a board game invented by Claude Soucie. It is played on a checkerboard with ordinary checkers pieces. The two players take turns, each moving a piece, and possibly capturing an opposing piece. The goal of the game is to get all of one’s pieces into one group of pieces that are connected. Two pieces are connected if they are adjacent horizontally, vertically, or diagonally. Initially, the pieces are arranged as shown below. Play alternates between Black and White, with Black moving first. Each move consists of moving a piece of your color horizontally, vertically, or diagonally onto an empty square or onto a square occupied by an opposing piece, which is then removed from the board. A piece may jump over friendly pieces (without disturbing them), but may not cross enemy pieces, except one that it captures. A piece must move a number of squares that is exactly equal to the total number of pieces (black and white) on the line along which it chooses to move (the *line of action*). This line contains both the squares behind and in front of the piece that moves, as well as the square the piece is on. A piece may not move off the board, onto another piece of its color, or over an opposing piece.



![image-20211027233731122](/Users/gongchen/Library/Application Support/typora-user-images/image-20211027233731122.png)





##### Starting a new game:

![image-20211027234305804](/Users/gongchen/Library/Application Support/typora-user-images/image-20211027234305804.png)



##### Switch machine player with human player:

![image-20211027234222495](/Users/gongchen/Library/Application Support/typora-user-images/image-20211027234222495.png)