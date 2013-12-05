(ns visualloy.core
  (:require [seesaw.core :refer [invoke-later]]
            [visualloy.alloy :refer [make-alloy print-alloy]]
            [visualloy.graphics :refer [array-canvas display canvas->frame
                                        temperature->color update-array-canvas]]
            [visualloy.physics :refer [update-alloy]])
  (:gen-class))

(def red [255 0 0])
(def yellow [255 255 0])

(defn run
  ""
  [thermal-constants height width top-left-temp bottom-right-temp]
  (let [alloy (make-alloy height width
                          top-left-temp bottom-right-temp
                          (count thermal-constants))
        [alloyA alloyB] (repeatedly 2 #(to-array-2d alloy))
;        alloyA (to-array-2d alloy)
;        alloyB (to-array-2d alloy)
;        T-max (* 2 (max top-left-temp bottom-right-temp)) ; make this better
        T-max Long/MAX_VALUE
        yellow-red-gradient #(temperature->color yellow red % T-max)
        transform #(yellow-red-gradient (:temp %))
        canvas (array-canvas alloyA transform)]
    (invoke-later (display (canvas->frame canvas "visualloy" height width)))
    (loop [input  alloyA
           output alloyB]
;      (println "update dat alloy shit")
      (update-alloy input output thermal-constants)
;      (println "now update dat canvas shit")
      (update-array-canvas canvas output transform)
;      (println "now lets get some recursion up in this bitch")
      (recur output input))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run [1.0 2.0 3.0] 100 100 10000000 50))
  ;; (let [thermal-constants [0.75 1.0 1.25]
  ;;       alloyA (make-alloy 10 20 (long 100) (long 20) (count thermal-constants))
  ;;       alloyB (aclone alloyA)]
  ;;   (dotimes [_ 1000]
  ;;     (print-alloy alloyA)
  ;;     (println "-------------------------------------")
  ;;     (update-alloy alloyA alloyB thermal-constants)
  ;;     (Thread/sleep 1)
  ;;     (print-alloy alloyB)
  ;;     (println "-------------------------------------")
  ;;     (update-alloy alloyB alloyA thermal-constants)
  ;;     (Thread/sleep 1))
  ;;   (print-alloy alloyA)))
