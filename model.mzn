include "alldifferent.mzn";

int: n;
int: regions_max_size;
int: num_of_regions;

set of int: dom = 1..n;
set of int: num_regions = 1..num_of_regions;

array[num_regions, dom] of var dom: matrix;
array[num_regions, 1..(regions_max_size*2 +2)] of int: regions;

constraint forall(i in dom) (
  alldifferent([matrix[i, j] | j in dom])
);

constraint forall(j in dom) (
  alldifferent([matrix[i, j] | i in dom])
);

constraint forall(i in 1..num_of_regions) (
  regions[i, 1] == sum(j in 1..regions[i, 2]) (matrix[regions[i, j*2 +1], regions[i, j*2 +2]])
);

output [
  show_int(2, matrix[i, j]) ++ if j == n then "\n" else " " endif | i in dom, j in dom
]