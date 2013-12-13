# visualloy

A clojure applet that simulates heat transfer across a metal alloy. Uses
divide-and-conquer parallelism to do the computation, subdividing the mesh until
reaching a user-defined area threshold. Cells in the mesh are mapped to pixels
in the display window, and their temperatures are mapped to an RGB value. Users
can customize which colors are used for the lower and upper temperature, and
colors in-between interpolate between the two. The maximum allowed temperature
is Long/MAX_VALUE (i.e. 9223372036854775807).

# Colors

* black
* white
* red
* yellow
* green
* blue

# Command Line Arguments

```
<height> <width> <threshold-area> <max-iterations> <T> <S> <default-temp>
<low-color> <high-color> <heat-transfer-coefficients>+
```

# Example Usage

```
java -jar visualloy.jar 128 128 64 1000 3073 373 273 yellow red 0.75 1.0 1.25
```
