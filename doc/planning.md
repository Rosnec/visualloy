# Initial Plan

Create some 2D array-like structure. Each cell contains a map of the form
{:temp ... :comp [...]}, where :temp is the temperature as a Long, and :comp
is a vector where the value at the ith index is the percentage of the cell
comprised of the ith metal.

Write a function which takes 2 arrays, and a vector of metal heat coefficients.
Apply another, smaller function, which operates on each cell individually,
writing the results to the corresponding cell in the other array. Returns some
number which represents the amount of change in this iteration, or maybe it
returns the array itself, or both arrays, or arrays and the Delta.

Write a never-ending function which keeps running the previously described
function and displaying the results as a heat map. Heat map only cares about
:temp attribute, not :comp.
