(ns visualloy.physics
  (:require [visualloy.alloy :refer [get-neighbors set-temperature]]
            [visualloy.util :refer [dimensions safe-add safe-multiply]]))

(declare time-step update-cell temp-from-neighbors)

; Rework this to run in parallel
(defn update-alloy
  "Updates the entire alloy to its next state, taking initial state from
  input-arr and writing the next state to output-arr"
  [input-arr output-arr thermal-constants]
  (let [[height width] (dimensions input-arr)
        first-index [0 0]
        last-index  [(dec height) (dec width)]]
    (doseq [row (range height) col (range width)
            :when (and (not= [row col] first-index)
                       (not= [row col] last-index))]
      (update-cell input-arr output-arr row col thermal-constants))
    output-arr))

(defn update-cell
  "Updates the temperature of the cell at the given index in the array.
  All writing is done on output-arr, and reading is done from input-arr."
  [input-arr output-arr row col thermal-constants]
  (let [neighbors (get-neighbors input-arr row col)
        temp (temp-from-neighbors neighbors thermal-constants)]
    (set-temperature output-arr row col temp)))

(defn temp-from-neighbors
  "Returns the temperature of a cell with the given neighbors.
  Maximum temperature is Long/MAX_VALUE, which is assured by dividing by
  the number of neighbors as early as possible, "
  [neighbors thermal-constants]
  (let [neighbor-divisor (/ (count neighbors))]
    (apply safe-add (for [metal (range (count thermal-constants))]
                      (long
                        (safe-multiply (nth thermal-constants metal)
                                       (apply + (for [cell neighbors]
                                                  (long
                                                   (* (:temp cell)
                                                      (nth (:comp cell) metal)
                                                      neighbor-divisor))))))))))
