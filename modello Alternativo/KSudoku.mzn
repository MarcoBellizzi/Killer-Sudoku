% killer sudoku solver
include "alldifferent.mzn";

int: R;  %in order: rows, colums, n. of groups, sudoku side, n. of elements 
int: C;
int: G;
int: L = R * C;
int: N= L *L;

int: digs = ceil(log(10.0, int2float(L))); % digits for output

set of int: PuzzleRange = 1..L;
set of int: SubSquareColRange = 1..C;
set of int: SubSquareRowRange = 1..R;

array[1..L, 1..L] of var PuzzleRange: puzzle;
array[1..N] of var PuzzleRange: puzzleLin;
array[1..G] of int : groupSums;
array[1..N] of 0..G : groups;

% Classical Sudoku constraints
    % all different in rows
    constraint forall(i,j in PuzzleRange)(
      alldifferent([puzzle[i,j] | j in PuzzleRange]));
    
    % all different in columns
    constraint forall(i,j in PuzzleRange)(
      alldifferent([puzzle[i,j] | i in PuzzleRange]));
    
    % all different in sub-squares
    constraint forall(a in SubSquareColRange, o in SubSquareRowRange)(
      alldifferent([puzzle[(a-1) *R + a1, (o-1)*C + o1] | a1 in SubSquareRowRange, o1 in SubSquareColRange]));

% Killer Sudoku constraints
    %filling the linearized version
     constraint forall(i,j in PuzzleRange)(
        puzzle[i,j] = puzzleLin[(i-1)*L+j] );
       
   %applying sum constraint 
    constraint forall(i in 1..G)(
      sum(j in 1..N where groups[j]==i)(puzzleLin[j])==groupSums[i]
    );

solve satisfy;

output [ 
  show_int(digs,puzzle[i,j]) ++ " " ++
    if j mod C == 0 then " " else "" endif ++
    if j == L /\ i != L then
      if i mod R == 0 then "\n\n" else "\n" endif
    else "" endif
  | i,j in PuzzleRange ] ++ 
  ["\n"];

