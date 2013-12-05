(ns visualloy.physics
  (:require [visualloy.alloy :refer [get-neighbors set-temperature]]
            [visualloy.util :refer [dimensions safe-add safe-multiply]]))

(declare update-cell temp-from-neighbors)

; Rework this to run in parallel
(defn update-alloy
  "Updates the entire alloy to its next state, taking initial state from
  input and writing the next state to output"
  [input output thermal-constants]
  (let [[height width] (dimensions input)
        first-index [0 0]
        last-index  [(dec height) (dec width)]]
    (doseq [row (range height) col (range width)
            :when (and (not= [row col] first-index)
                       (not= [row col] last-index))]
      (update-cell input output row col thermal-constants))
    output))

(defn update-cell
  "Updates the temperature of the cell at the given index in the array.
  All writing is done on output, and reading is done from input."
  [input output row col thermal-constants]
  (let [neighbors (get-neighbors input row col)
        temp (temp-from-neighbors neighbors thermal-constants)]
    (set-temperature output row col temp)))

(defn temp-from-neighbors
  "Returns the temperature of a cell with the given neighbors.
  Maximum temperature is Long/MAX_VALUE, which is assured by dividing by
  the number of neighbors as early as possible, "
  [neighbors thermal-constants]
  (let [neighbor-divisor (/ (count neighbors))]
    (apply safe-add
           (for [m (range (count thermal-constants))
                 :let [C_m (nth thermal-constants m)]]
             (long
              (safe-multiply C_m
                             (apply safe-add (for [cell neighbors
                                            :let [T (:temp cell)
                                                  p_m (nth (:comp cell) m)]]
                                        (long
                                         (* T p_m neighbor-divisor))))))))))
