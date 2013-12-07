# Initial Plan for Assignment 3

Create some 2D array-like structure. Each cell contains a map of the form
{:temp ..., :comp [...]}, where :temp is the temperature as a Long, and :comp
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



# Future Plan for Assignment 4

Instead of drawing after each step completes, run a daemon thread which does a
repaint, and have the paint function use seesaw.bind/bind to bind each cell to a
ref, which is inside the paint function. This will cause updating the screen and
updating the mesh to occur asynchronously. Since we are swapping arrays, and can
only easily really bind one value (tee would make it possible to bind both
meshes, but that's not necessary), only one array needs to be bound to the GUI.
Since everything is happening asynchronously, it won't really matter whether we
use one array or both.

My understanding of seesaw.bind/bind as written above is probably wrong. I think
what I really need to do is subdivide the mesh into separate canvases for each
parallel machine, and bind the :paint property to a ref of some lambda function
which returns the graphics to paint. Running update-alloy will alter that ref.
Consider pros and cons of using refs vs other reference types, but refs should
do. I'm not sure if agents can be bound or not, but they might be best if they
can be.
