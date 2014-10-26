(ns loopitude.piano-roll
  (:require [reagent.core :as reagent]))

(def rows 25)
(def default-row-offset (- (.ceil js/Math (/ rows 2)))) ;; 0 is middle C, centered at 0
(def cols 32)

(defn piano-roll [{:keys [notes row-offset playing-col]}]
  (let [notes' @notes]
    [:table.piano-roll
     (for [row (range rows)
           :let [note (+ row default-row-offset row-offset)]]
       [:tr {:key row}
        (for [col (range cols)
              :let [on (contains? (notes' col) note)]]
          [:td {:class (str (when on "on ")
                            (when (= col playing-col) "playing"))
                :key col
                :on-click (fn []
                            (reset! notes
                                    (update-in notes' [col]
                                               #((if on disj conj)
                                                 (or % #{})
                                                 note))))}])])]))
