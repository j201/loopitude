(ns loopitude.piano-roll
  (:require [reagent.core :as reagent]))

(def rows 25)
(def default-row-offset (- (.ceil js/Math (/ rows 2)))) ;; 0 is middle C, centered at 0
(def cols 16)

(defn piano-row [{:keys [row notes row-offset playing-col]}]
  (let [notes' @notes
        note (+ (- rows row) default-row-offset @row-offset)]
    [:tr (for [col (range cols)
               :let [on (contains? (notes' col) note)]]
           ^{:key col}
           [:td {:class (str (when on "on ")
                             (when (= col playing-col) "playing ")
                             (when (= -1 (+ row default-row-offset)) "middle"))
                 :key col
                 :on-mouse-down (fn []
                                  (swap! notes
                                         (fn [notes] (update-in notes [col]
                                                                #((if on disj conj)
                                                                  (or % #{})
                                                                  note)))))}])]))

(defn piano-roll [{:keys [notes row-offset playing-col]}]
  (let [notes' @notes]
    [:table.piano-roll
     (for [row (range rows)]
       ^{:key row}
       [piano-row {:row row
                   :notes notes
                   :row-offset row-offset
                   :playing-col playing-col}])]))
