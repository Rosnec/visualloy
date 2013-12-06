(ns visualloy.graphics
  (:require [seesaw.color :refer [color]]
            [seesaw.core :refer [canvas config! frame pack! show!]]
            [seesaw.graphics :refer [draw path style]]
            [visualloy.util :refer [dimensions interpolate-value]]))

(defn display
  "Display a frame"
  [frame]
  (-> frame
 ;     pack!
      show!))


(defn canvas->frame
  "Puts canvas in a frame"
  [c name height width]
  (frame :title name :size [width :by height] :content c
         :resizable? false :on-close :exit))

(defn pixel
  "Draws a single pixel at the point specified."
  [x y]
  (path []
    (move-to x y)
    (line-to x y)))

(defn draw-array-to-graphics
  "Draws an array to a graphics object by applying the transformation to each
  point in the array, and using the result of that transformation as the
  parameters to apply to color for the corresponding rectangle in the
  graphics. width and height are integers which define the dimensions of each
  pixel in the graphics."
  [graphics arr transform]
  (let [[h w] (dimensions arr)]
    (apply draw graphics
           (apply concat
             (for [row (range h) col (range w)]
               [(pixel col row)
                (style :foreground (apply color
                                          (transform (aget arr row col))))]))))
    ;; (doseq [row (range h) col (range w)]
    ;;   (draw graphics
    ;;         (pixel col row)
    ;;         (style :background (apply color (transform (aget arr row col)))))))
  graphics) ; either hangs up in this function, or after it

(defn array-canvas
  "Makes a canvas from an alloy array."
  [array transform]
  (canvas :id :canvas :background "#000FFF"
          :paint (fn [c g] (draw-array-to-graphics g array transform))))

(defn update-array-canvas
  "Updates an array-canvas with the provided array and transformation."
  [canvas array transform]
  (config! canvas :paint (fn [c g] (draw-array-to-graphics g array transform))))

(defn temperature->color
  "Given two [R G B] vectors, low-color and high-color, as well as the current
  temperature T and the max temperature T-max, returns a color that is
  T/T-max of the way from low-color to high-color."
  [^clojure.lang.PersistentVector low-color
   ^clojure.lang.PersistentVector high-color
   T T-max]
;  (println "val" T "max" T-max "p" (/ T T-max))
  (let [portion (/ T T-max)]
    (for [[low high] (partition 2 (interleave low-color high-color))]
      (let [c (int (interpolate-value low high portion))]
;        (println "T" T "p" portion "color" c)
        c))))

(defn pixel
  "Draws a single pixel at the point specified."
  [x y]
  (path []
    (move-to x y)
    (line-to x y)))
