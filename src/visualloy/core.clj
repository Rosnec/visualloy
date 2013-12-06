(ns visualloy.core
  (:require [seesaw.core :refer [config! frame invoke-later]]
            [visualloy.alloy :refer [make-alloy print-alloy]]
            [visualloy.graphics :refer [array-canvas display canvas->frame
                                        temperature->color update-array-canvas]]
            [visualloy.physics :refer [update-alloy]]
            [visualloy.util :refer [mean]])
  (:gen-class))

; some colors

(def black  [0   0   0  ])
(def white  [255 255 255])
(def red    [255 0   0  ])
(def yellow [255 255 0  ])
(def green  [0   255 0  ])
(def blue   [0   0   255])

(defn run
  [thermal-constants height width threshold max-iterations
   top-corner-temp bot-corner-temp]
  (let [alloy (make-alloy height width
                          top-corner-temp bot-corner-temp
                          (count thermal-constants))
        [alloyA alloyB] (repeatedly 2 #(to-array-2d alloy))
        max-starting-temp (max top-corner-temp bot-corner-temp)
        avg-thermal-constant (mean thermal-constants)
        T-max (long; (+ (int (/ max-starting-temp 100))
                       (* max-starting-temp avg-thermal-constant));)
;        transform #(temperature->color yellow red % T-max)
        transform #(temperature->color black white % T-max)
        canvas (array-canvas alloyA transform)
        f (canvas->frame canvas "visualloy" (+ height 21) (+ width 1))]
    (invoke-later (display f))
    (loop [input  alloyA
           output alloyB
	   iterations 0]
      (if (< iterations max-iterations)
        (do
         (config! canvas :paint (update-alloy input output
                                              top-corner-temp bot-corner-temp
                                              thermal-constants transform
                                              threshold))
         (recur output input (inc iterations)))
        (println "Reached max iterations")))))

(defn -main
  "I don't do a whole lot ... yet."
  [height width threshold max-iterations T S & coefficients]
  (run (map #(Double. %) coefficients)
       (Integer. height) (Integer. width)
       (Integer. threshold) (Long. max-iterations)
       (Long. T) (Long. S)))
