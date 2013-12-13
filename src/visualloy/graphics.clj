(ns visualloy.graphics
  (:require [seesaw.color :refer [color]]
            [seesaw.core :refer [canvas config! frame pack! show!]]
            [seesaw.graphics :refer [draw path style]]
            [visualloy.util :refer [dimensions interpolate-value nth-deep]]))

(defn display
  "Display a frame"
  [frame]
  (-> frame
 ;     pack!
      show!))

(defn update-image
  "Updates a buffered image with the given pixel array. Returns nil."
  [image temp-cells transform]
;  (println (:index (first temp-cells))) (System/exit 0)
  (dorun
  (pmap (fn [cell] (apply #(.setRGB image %2 % (transform @(:temperature cell)))
                          (:index cell)))
        temp-cells)))

(defn draw-daemon
  ""
  [panel image input height width transform]
  (let [temp-cells (vec (for [cell (flatten input)]
                          {:temperature (:temperature cell),
                           :index       (:index       cell)}))]
    (loop []
;(println "update")
      (update-image image temp-cells transform)
;(println "repaint")
      (.repaint panel)
      (Thread/sleep 50)
      (recur))))

(defn temperature->color
  "Given two [R G B] vectors, low-color and high-color, as well as the current
  temperature T and the max temperature T-max, returns a color that is
  T/T-max of the way from low-color to high-color."
  [^clojure.lang.PersistentVector low-color
   ^clojure.lang.PersistentVector high-color
   T T-max]
  (let [portion (/ T T-max)]
    (for [[low high] (partition 2 (interleave low-color high-color))]
      (let [c (int (interpolate-value low high portion))]
        c))))