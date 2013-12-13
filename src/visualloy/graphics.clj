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


(defn canvas->frame
  "Puts canvas in a frame"
  [c name height width]
  (frame :title name :size [width :by height] :content c
         :on-close :exit))

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

(defn make-painter
  "Returns a daemon function to be used as a canvas's :paint function"
  [array transform]
  (let [[height width] (dimensions array)]
    (fn painter [canvas graphics]
      (apply draw graphics
        (apply concat
          (for [row (range height)
                col (range width)
                :let [index [row col]]]
            [(pixel col row)
             (style :foreground (apply color
                                  (transform (nth-deep array index))))]))))))



(defn update-image
  "Updates a buffered image with the given pixel array. Returns nil."
  [image temp-agents height width transform]
;  (println (transform @(first (first temp-agents))))
;  (.setRGB image 0 0 (transform @(nth-deep temp-agents [0 0]))))
  (doseq [row (range height) col (range width)]
      (.setRGB image col row (transform @(nth (nth temp-agents row) col)))))

;  (dorun (pmap (fn [index-row temp-agent-row]
;                 (pmap (fn [index temp-agent]
;		         (do (println index (transform @temp-agent))
;                         (apply #(.setRGB image %2 % (transform @temp-agent))
;			        index)
;                         )
;                       index-row temp-agent-row))
;               indices temp-agents))))

(defn draw-daemon
  ""
  [panel image input height width transform]
  (let [temp-agents (vec (for [row input] (vec (pmap #(:temperature %) row))))
        indices (vec (for [row (range height)]
                  (vec (for [col (range width)] [row col]))))
    (loop []
(println "update")
      (update-image image temp-agents height width transform)
(println "repaint")
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
;        (println "T" T "p" portion "color" c)
        c))))
