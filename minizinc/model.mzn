include "alldifferent.mzn";

int: n;
int: m;
int: regions_max_size;
int: num_of_regions;

array[1..num_of_regions, 1..(regions_max_size * 2 + 2)] of int: regions;

set of int: dom = 0..9;
array[1..n*m, 1..n*m] of dom : matrix;

constraint forall(i in 1..n*m) (
  alldifferent([matrix[i, j] | j in 1..n*m])
);

constraint forall(j in 1..n*m) (
  alldifferent([matrix[i, j] | i in 1..n*m])
);

constraint forall(I in 0..m-1, J in 0..n-1) (
  alldifferent([matrix[i + (I*n), j + (J*m)] | i in 1..n, j in 1..m])
);

constraint forall(i in 1..num_of_regions) (
  regions[i, 1] == sum(j in 1..regions[i, 2]) (matrix[regions[i, j*2 +1], regions[i, j*2 +2]])
);

output [
"GOOD JOB!"
]