(ns visualloy.core
  (:require [visualloy.alloy :refer [make-alloy print-alloy]]
            [visualloy.graphics :refer [array-canvas display canvas->frame
                                        temperature->color update-array-canvas]]
            [visualloy.physics :refer [update-alloy]])
  (:gen-class))

(def red [255 0 0])
(def yellow [255 255 0])

(defn run
  ""
  [thermal-constants height width top-left-temp bottom-right-temp]
  (let [alloyA (make-alloy height width
                           top-left-temp bottom-right-temp
                           (count thermal-constants))
        alloyB (aclone alloyA)
        T-max (* 4 (max top-left-temp bottom-right-temp)) ; make this better
        yellow-red-gradient #(temperature->color yellow red % T-max)
        transform #(yellow-red-gradient (:temp %))
        canvas (array-canvas alloyA transform)]
    (display (canvas->frame canvas "visualloy" height width))
    (loop [input  alloyA
           output alloyB]
      (update-alloy input output thermal-constants)
      (update-array-canvas canvas output transform)
      (Thread/sleep 1000)
      (recur output input))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run [0.75 1.0 1.25] 32 64 100 20))
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
