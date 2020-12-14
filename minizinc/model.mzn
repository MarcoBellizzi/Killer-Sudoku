include "alldifferent.mzn";

int: n;
int: m;
int: regions_max_size;
int: num_of_regions;

array[1..num_of_regions, 1..(regions_max_size * 2 + 2)] of int: regions;

set of int: dom = 1..9;
array[1..n*m, 1..n*m] of dom : matrix;

constraint assert(forall(i in 1..n*m, j in 1..n*m) (  % user input validation
    matrix[i,j] > 0 /\ matrix[i,j] <= n*m
), "wrong user input value");

constraint assert(forall(i in 1..num_of_regions) (   % site input validation
  forall(j in 1..regions[1, 2]) (
    regions[i, j*2 +1] > 0 /\ regions[i, j*2 +2] <= n*m
  )
), "wrong site input value");

constraint forall(i in 1..n*m) (    % elements in each row are different
  alldifferent([matrix[i, j] | j in 1..n*m])
);

constraint forall(j in 1..n*m) (    % elements in each column are different
  alldifferent([matrix[i, j] | i in 1..n*m])
);

constraint forall(I in 0..m-1, J in 0..n-1) (  % elements in each sector are different
  alldifferent([matrix[i + (I*n), j + (J*m)] | i in 1..n, j in 1..m])
);

constraint forall(i in 1..num_of_regions) (   % elements in each region are different
  forall(j in 1..regions[i, 2]) (
    alldifferent([matrix[regions[i, j*2 +1], regions[i, j*2 +2]]])
  )
);

constraint forall(i in 1..num_of_regions) (   % the sum of each region is equal to the sum of each element in that region
  regions[i, 1] == sum(j in 1..regions[i, 2]) (matrix[regions[i, j*2 +1], regions[i, j*2 +2]])
);

output [
"GOOD JOB!"
]