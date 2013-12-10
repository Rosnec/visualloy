(ns visualloy.alloy
  (:require [visualloy.util :refer [dimensions nth-deep
                                    random-float-portions]]))

(declare make-alloy make-cell set-temperature get-neighbors)

(def neighbor-positions
       [[-1 0]
  [0 -1]      [0 1]
        [ 1 0]])



(defstruct alloy-cell :temp :comp :neighbors)

(defn make-cell
  "Makes a cell with the given number of types and given starting temperature"
  [types T] (alloy-cell :temp T :comp (random-float-portions 1.0 types)))

(defn get-neigbors
  "Returns the cells which neighbor the cell at index in alloy"
  [alloy height width index]
  (for [delta neighbor-deltas
        :let [index (map + index delta)]
        :when (and (every? #(not (neg? %)))
                   (< (first index) height)
                   (< (second index) width))]
    (nth-deep alloy index)))

(defn assoc-neighbors
  "Associates the neighboring cells to each cell in alloy"
  [alloy height width]
  (doseq [row (range height) col (range width)
          :let [index [row col]]]
    (send (nth-deep alloy index)
          assoc
          :neighbors (get-neighbors alloy height width index))))

(defn make-alloy
  [height width types first-temp last-temp default-temp]
  (let [first-index [0 0]
        last-index  [(dec height) (dec width)]
        alloy
        (vec (for [row (range height)]
               (for [col (range width)
                     :let [index [row col]]]
                 (agent (cond (= index first-index) (make-cell types first-temp)
                              (= index last-index)  (make-cell types last-temp)
                              :else (make-cell types default-temp))))))]
    (assoc-neighbors alloy height width)))

; stuff below is either redundant or hasn't been updated yet
; rest of the program hasn't been updated yet either


(defn make-alloy
  "Create a lazy sequence which represents an alloy with the given parameters.
  All cells start with temperature 0, except for the top-left and bottom-right
  cells, which have a user-defined temperature which remains constant.

  Parameters:
  height            - number of rows of the alloy
  width             - number of columns of the alloy
  top-left-temp     - temperature of top-left cell
  bottom-right-temp - temperature of bottom-right cell
  metal-types       - number of different types of base metals"
  [^java.lang.Integer height        ^java.lang.Integer width
   ^java.lang.Long    top-left-temp ^java.lang.Long    bottom-right-temp
   ^java.lang.Integer metal-types   ^java.lang.Integer base-temp]
  (for [row (range height)]
    (for [col (range width)]
      (cond (= [row col] [0 0]) (make-cell metal-types top-left-temp)
            (= [row col] [(dec height) (dec width)])
            (make-cell metal-types bottom-right-temp)
            :else (make-cell metal-types base-temp)))))

(defn make-cell
  "Makes a cell with the given number of base metal types and (optional) initial
  temperature."
  ([metal-types]
     (make-cell metal-types 0))
  ([metal-types temp]
     {:temp (long temp)
      :comp (random-float-portions 1 metal-types)}))

(defn set-temperature
  "Sets the temperature at the given index of the array"
  [arr row col temp]
  (let [prev (aget arr row col)]
    (aset arr row col
          (assoc prev :temp temp)))
  temp)
;          {:temp temp
;           :comp (:comp prev)})))

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
