(ns visualloy.core
  (:require [seesaw.core :refer [frame invoke-later show!]]
            [seesaw.color :refer [color]]
	    [seesaw.graphics :refer [buffered-image]]
            [visualloy.alloy :refer [make-mirrored-alloys update-alloy]]
            [visualloy.graphics :refer [draw-daemon temperature->color]]
            [visualloy.pool :refer [new-pool]]
            [visualloy.util :refer [daemon interpolate-value mean]])
  (:import (java.awt.image BufferedImage)
           (javax.swing JPanel))
  (:gen-class))

; some colors

(def black  [  0   0   0])
(def white  [255 255 255])
(def red    [255   0   0])
(def yellow [255 255   0])
(def green  [  0 255   0])
(def blue   [  0   0 255])

(def color-map
  {"black"  black,
   "white"  white,
   "red"    red,
   "yellow" yellow,
   "green"  green,
   "blue"   blue})

(defn run
  [thermal-constants height width threshold max-iterations
   top-corner-temp bot-corner-temp default-temp low-color high-color]
  (let [[alloyA alloyB] (make-mirrored-alloys height width
                                              (count thermal-constants)
                                              top-corner-temp bot-corner-temp
                                              default-temp)
        max-starting-temp (max top-corner-temp bot-corner-temp default-temp)
        ; Sets the temperature corresponding to high-color by multiplying
        ; the average thermal-constant value by the greatest of the starting
        ; temperatures
        avg-thermal-constant (mean thermal-constants)
        T-max (long (* max-starting-temp avg-thermal-constant))
        transform #(.getRGB (apply color
	                           (temperature->color low-color high-color
                                                       % T-max)))
        bg-color (color "blue")
	image (buffered-image width height BufferedImage/TYPE_INT_ARGB)
	panel (proxy [JPanel] [] (paintComponent [g] (.drawImage g image 0 0
	                                                bg-color nil)))
        f (frame :title "visualloy" :content panel
	         :size [width :by height]
		 :on-close :exit)
        pool (new-pool)
	start-time (System/nanoTime)]
    (invoke-later (show! f))
    (daemon #(draw-daemon panel image alloyA height width transform))
    (loop [input  alloyA
           output alloyB
	   iterations 0]
      (if (< iterations max-iterations)
        (do (update-alloy pool input height width thermal-constants threshold)
            (recur output input (inc iterations)))
        (println "Reached max iterations after"
	         (int (/ (- (System/nanoTime) start-time)
      		         1000000000))
		 "seconds.")))))

(defn -main
  ([] (println (str "Arguments: <height> <width> <threshold-area>"
                              " <max-iterations> <T> <S> <default-temp>"
                              " <low-color> <high-color>"
                              " <heat-transfer-coefficients>+")))
  ([height width threshold max-iterations T S default-temp
    low-color high-color & coefficients]
    (if (empty? coefficients)
      (-main)
      (run (map #(Double. %) coefficients)
           (Integer. height) (Integer. width) (Integer. threshold)
           (Long. max-iterations)
           (Long. T) (Long. S) (Long. default-temp)
           (color-map low-color) (color-map high-color)))))
