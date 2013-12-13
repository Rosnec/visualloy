(ns visualloy.alloy
  (:require [visualloy.pool :as pool]
            [visualloy.util :refer [area dimensions
	    		    	    midpoint nth-deep
                                    random-float-portions
                                    safe-add safe-multiply]]))

(declare make-alloy make-cell set-temperature get-neighbors)

(def neighbor-deltas
      [[-1 0]
  [0 -1]    [0 1]
       [ 1 0]])

(defstruct alloy-cell :temperature :mirror-temp :composition :neighbors
                      :index :is-source?)

(defn make-temperature-agents
  "Makes a 2D collection of temperature atoms to be placed into the cells"
  [height width first-index last-index
   top-left-temp bot-right-temp default-temp]
  (for [row (range height)]
    (for [col (range width)
          :let [index [row col]]]
      (agent
       (cond
        (= index first-index) top-left-temp
        (= index last-index)  bot-right-temp
        :else                 default-temp)))))

(defn make-compositions
  "Makes a 2D collection of compositions to be placed into the cells"
  [height width types]
  (for [_ (range height)]
    (for [_ (range width)]
      (random-float-portions 1.0 types))))

(defn make-cell
  "Makes a cell with the given number of types and given starting temperature"
  [index neighbor-indices temperature-coll mirror-temp-coll composition-coll
   is-source?]
  (let [[temperature mirror-temp] (pmap #(nth-deep % index) [temperature-coll
				                             mirror-temp-coll
				                             composition-coll])
        neighbors (for [n neighbor-indices]
                    {:temperature (nth-deep temperature-coll n)
		     :composition (nth-deep composition-coll n)})]
    (struct-map alloy-cell
      :temperature temperature
      :mirror-temp mirror-temp
      :neighbors   neighbors
      :index       index
      :is-source?  is-source?)))

(defn get-neighbor-indices
  "Returns the cells which neighbor the cell at index in alloy"
  [height width index]
  (let [dim [height width]]
    (for [delta neighbor-deltas
            :let [index (map + index delta)]
            :when (and (not-any? neg? index)
                       (every? true? (map < index dim)))]
      index)))

(defn make-alloy
  "Makes an alloy"
  [height width first-index last-index
   temperature-coll mirror-temp-coll composition-coll]
  (for [row (range height)]
    (for [col (range width)
          :let [index [row col]
                is-source? (if (or (= index first-index) (= index last-index))
                               true false)
                neighbor-indices (get-neighbor-indices height width index)]]
      (make-cell index neighbor-indices
                 temperature-coll mirror-temp-coll composition-coll
                 is-source?))))

(defn make-mirrored-alloys
  [height width types top-left-temp bot-right-temp default-temp]
  (let [first-index [0 0]
        last-index [(dec height) (dec width)]
        [temp-collA temp-collB]
        (repeatedly 2 #(make-temperature-agents height width
	                                        first-index last-index
                                                top-left-temp bot-right-temp
                                                default-temp))
        composition-coll (make-compositions height width types)
        [alloyA alloyB]
	(pmap (fn [[temperature-coll mirror-temp-coll]]
                (make-alloy height width first-index last-index
                            temperature-coll mirror-temp-coll
			    composition-coll))
              [[temp-collA temp-collB] [temp-collB temp-collA]])]
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
                                      :let [T @(:temperature cell)
                                            p_m (nth (:composition cell) m)]]
                                      (long (* T p_m neighbor-divisor))))))))))

(defn update-cell
  "Updates the cell which mirrors the given cell"
  [cell thermal-constants]
  (when (not (:is-source? cell))
    (send (:mirror-temp cell)
          (fn [T] (temp-from-neighbors (:neighbors cell) thermal-constants)))))

(defn new-alloy-task
  [& args]
  (pool/new-task (fn [_] (apply alloy-task args))))

(defn alloy-task
  [alloy first-row last-row first-col last-col thermal-constants threshold]
  (if (<= (area first-row last-row first-col last-col) threshold)
    (compute-region alloy first-row last-row first-col last-col
                    thermal-constants)
    (let [mid-row (midpoint first-row last-row)
          mid-col (midpoint first-col last-col)
          top-left
          (pool/fork-task
           (new-alloy-task alloy first-row mid-row first-col mid-col
                           thermal-constants threshold))
          top-right
          (pool/fork-task
           (new-alloy-task alloy first-row mid-row mid-col last-col
                           thermal-constants threshold))
          bot-left
          (pool/fork-task
           (new-alloy-task alloy mid-row last-row first-col mid-col
                           thermal-constants threshold))
          bot-right
          (pool/compute-task
           (new-alloy-task alloy mid-row last-row mid-col last-col
                           thermal-constants threshold))]
      nil)))

(defn compute-region
  [alloy first-row last-row first-col last-col thermal-constants]
  (doseq [row (range first-row last-row)
          col (range first-col last-col)
          :let [index [row col]]
          :when (and (not= index top-corner-index)
                     (not= index bot-corner-index))]
    (update-cell input output row col thermal-constants)))

(defn update-alloy
  "Updates every cell in the alloy mirroring the given alloy"
  [alloy thermal-constants]
  (let [update-fn #(update-cell % thermal-constants)]
    (dorun (pmap update-fn (flatten alloy)))))

(defn show-alloy
  "Returns a 2D sequence of the temperatures for each cell in the alloy"
  [alloy]
  (let [[height width] (dimensions alloy)]
    (for [row (range height)]
      (for [col (range width)]
        @(:temperature (nth-deep alloy [row col]))))))

(defn print-alloy
  "Prints the 2D sequence given by show-alloy"
  [alloy]
  (doseq [row (show-alloy alloy)]
    (println row)))
