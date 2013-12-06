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
  ""
  [thermal-constants height width top-corner-temp bot-corner-temp]
  (let [alloy (make-alloy height width
                          top-corner-temp bot-corner-temp
                          (count thermal-constants))
        [alloyA alloyB] (repeatedly 2 #(to-array-2d alloy))
        max-starting-temp (max top-corner-temp bot-corner-temp)
        avg-thermal-constant (mean thermal-constants)
        T-max (long (Math/pow max-starting-temp avg-thermal-constant))
;        T-max (* 2 (max top-left-temp bottom-right-temp)) ; make this better
;        T-max Long/MAX_VALUE
        transform #(temperature->color yellow red % T-max)
        canvas (array-canvas alloyA transform)
        threshold 16
        f (canvas->frame canvas "visualloy" (+ height 21) (+ width 1))]
    (invoke-later (display f))
    (loop [input  alloyA
           output alloyB]
      (config! canvas :paint (update-alloy input output thermal-constants
                                           top-corner-temp bot-corner-temp
                                           transform threshold))
      (recur output input))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run [0.75 1.0 1.25] 16 16 1000 50))
