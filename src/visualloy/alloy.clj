(ns visualloy.alloy
  (:require [visualloy.util :refer [dimensions random-float-portions]]))

(declare make-alloy make-cell set-temperature get-neighbors)

(defn make-alloy
  "Initialize an alloy array with the given parameters.
  All cells start with temperature 0, except for the top-left and bottom-right
  cells, which have a user-defined temperature which remains constant.

  Parameters:
  height            - number of rows of the array
  width             - number of columns of the array
  top-left-temp     - temperature of top-left cell
  bottom-right-temp - temperature of bottom-right cell
  metal-types       - number of different types of base metals"
  [^java.lang.Integer height        ^java.lang.Integer width
   ^java.lang.Long    top-left-temp ^java.lang.Long    bottom-right-temp
   ^java.lang.Integer metal-types]
  (let [arr (make-array clojure.lang.PersistentArrayMap height width)]
    (doseq [row (range height)
            col (range width)]
      (aset arr row col (make-cell metal-types)))
    (set-temperature arr 0      0     top-left-temp)
    (set-temperature arr
                     (dec height)
                     (dec width) bottom-right-temp)
    arr))

(defn make-cell
  "Makes a cell with the given number of base metal types"
  [metal-types]
  {:temp (long 0)
   :comp (random-float-portions 1 metal-types)})

(defn set-temperature
  "Sets the temperature at the given index of the array"
  [arr row col temp]
  (let [prev (aget arr row col)]
    (aset arr row col
          {:temp temp
           :comp (:comp prev)})))

(defn get-neighbors
  "Returns a sequence of the cells above, below, and to the left and right of
  the cell at the given index in the array. Sequence should have length between
  2 and 4."
  [arr row col]
  (let [[height width] (dimensions arr)
        indices (for [dh [-1 0 1]
                      dw [-1 0 1]
                      :when (not= (Math/abs dh) (Math/abs dw))]
                  [(+ row dh) (+ col dw)])]
    (for [[row col] indices
          :when (and (>= row 0)
                     (>= col 0)
                     (< row height)
                     (< col width))]
      (aget arr row col))))

(defn show-alloy
  "Returns a 2D sequence of the temperatures for each cell in the alloy"
  [arr]
  (let [[height width] (dimensions arr)]
    (for [row (range height)]
      (for [col (range width)]
        (:temp (aget arr row col))))))

(defn print-alloy
  "Prints the 2D sequence given by show-alloy"
  [arr]
  (doseq [row (show-alloy arr)]
    (println row)))
