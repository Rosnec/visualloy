(ns visualloy.alloy
  (:require [clojure.core.reducers :as r]
            [visualloy.util :refer [dimensions nth-deep
                                    random-float-portions
                                    nth-deep
                                    safe-add safe-multiply]]))

(declare make-alloy make-cell set-temperature get-neighbors)

(def neighbor-deltas
      [[-1 0]
  [0 -1]    [0 1]
       [ 1 0]])

(defstruct alloy-cell :temperature :composition :neighbors :mirror-cell
                      :is-source?)

(defn make-cell
  "Makes a cell with the given number of types and given starting temperature"
  [T composition is-source?]
  (struct-map alloy-cell
    :temperature T
    :composition composition
    :is-source? is-source?))

(defn get-neighbors
  "Returns the cells which neighbor the cell at index in alloy"
  [alloy height width index]
  (let [dim [height width]]
    (for [delta neighbor-deltas
            :let [index (map + index delta)]
            :when (and (not-any? neg? index)
                       (every? true? (map < index dim)))]
;      (do (println (:temperature @(nth-deep alloy index)))
;          (System/exit 0)
      (nth-deep alloy index))))
;)
(defn assoc-neighbors
  "Associates the neighboring cells to each cell in alloy"
  [alloy height width]
  (doseq [row (range height) col (range width)
          :let [index [row col]]]
    (send (nth-deep alloy index)
          assoc
          :neighbors (get-neighbors alloy height width index))))

(defn mirror-alloys
  "Links corresponding cells in two alloys of the same size"
  [alloyA alloyB]
  (let [[height width] (dimensions alloyA)]
    (doseq [row (range height)
            col (range width)
            :let [index [row col]
                  cellA (nth-deep alloyA index)
                  cellB (nth-deep alloyB index)]]
      (send cellA assoc :mirror-cell cellB)
      (send cellB assoc :mirror-cell cellA))))

(defn make-mirrored-alloys
  [height width types first-temp last-temp default-temp]
  (let [first-index [0 0]
        last-index [(dec height) (dec width)]
        compositions (vec (for [row (range height)]
                            (for [col (range width)]
                              (random-float-portions 1.0 types))))
        [alloyA alloyB]
        (repeatedly 2 #(vec (for [row (range height)]
                              (for [col (range width)
                                :let [index [row col]
                                      [temp is-source?]
                                      (cond
                                       (= index first-index) [first-temp true]
                                       (= index last-index) [last-temp true]
                                       :else [default-temp false])]]
                                (agent
                                 (make-cell temp
                                            (nth-deep compositions index)
                                            is-source?))))))]
    (mirror-alloys alloyA alloyB)
    (dorun (pmap #(assoc-neighbors % height width) [alloyA alloyB]))
    ; Wait until all cells have been set up before returning
    (doseq [cell (flatten (concat alloyA alloyB))] (await cell))
    [alloyA alloyB]))

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
                             (apply safe-add
                                    (for [cell neighbors
                                      :let [T (:temperature cell)
                                            p_m (nth (:composition cell) m)]]
                                      (long (* T p_m neighbor-divisor))))))))))

(defn update-cell
  "Updates the cell which mirrors the given cell"
  [cell thermal-constants]
  (when (not (:is-source? @cell))
    (send (:mirror-cell @cell)
          assoc :temperature (temp-from-neighbors (map deref
                                                       (:neighbors @cell))
                                                  thermal-constants))))

(defn update-alloy
  "Updates every cell in the alloy mirroring the given alloy"
  [alloy thermal-constants]
  (let [update-fn #(update-cell % thermal-constants)]
    ; probably should use something other than pmap
    (dorun (pmap update-fn (flatten alloy)))))

(defn show-alloy
  "Returns a 2D sequence of the temperatures for each cell in the alloy"
  [alloy]
  (let [[height width] (dimensions alloy)]
    (for [row (range height)]
      (for [col (range width)]
        (:temperature @(nth-deep alloy [row col]))))))

(defn print-alloy
  "Prints the 2D sequence given by show-alloy"
  [alloy]
  (doseq [row (show-alloy alloy)]
    (println row)))
