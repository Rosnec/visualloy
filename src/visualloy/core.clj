(ns visualloy.core
  (:require [seesaw.core :refer [config! frame invoke-later]]
            [seesaw.color :refer [color]]
	    [seesaw.graphics :refer [buffered-image]]
            [visualloy.alloy :refer [make-mirrored-alloys update-alloy
                                     print-alloy]]
            [visualloy.graphics :refer [display draw-daemon temperature->color]]
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

(defn run
  [thermal-constants height width max-iterations
   top-corner-temp bot-corner-temp default-temp]
  (let [[alloyA alloyB] (make-mirrored-alloys height width
                                              (count thermal-constants)
                                              top-corner-temp bot-corner-temp
                                              default-temp)
        max-starting-temp (max top-corner-temp bot-corner-temp default-temp)
        avg-thermal-constant (mean thermal-constants)
        T-max (long (* max-starting-temp avg-thermal-constant))
        transform #(.getRGB (apply color
	                           (temperature->color black white % T-max)))
        bg-color (color "blue")
	image (buffered-image width height BufferedImage/TYPE_INT_ARGB)
	panel (proxy [JPanel] [] (paintComponent [g] (.drawImage g image 0 0
	                                                bg-color nil)))
        f (frame :title "visualloy" :content panel
	         :size [width :by height]
		 :on-close :exit)
	start-time (System/nanoTime)]
;    (println max-starting-temp avg-thermal-constant T-max)
    (invoke-later (display f))
    (daemon (draw-daemon panel image alloyA height width transform))
    (loop [input  alloyA
           output alloyB
	   iterations 0]
      (if (< iterations max-iterations)
        (do (update-alloy input thermal-constants)
            (recur output input (inc iterations)))
        (println "Reached max iterations after"
	         (int (/ (- (System/nanoTime) start-time)
      		         1000000000))
		 "seconds.")))))

(defn -main
  "I don't do a whole lot ... yet."
  [height width max-iterations T S default-temp & coefficients]
  (run (map #(Double. %) coefficients)
       (Integer. height) (Integer. width)
       (Long. max-iterations)
       (Long. T) (Long. S) (Long. default-temp)))
