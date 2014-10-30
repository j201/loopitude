(ns loopitude.piano-roll
  (:require [reagent.core :as reagent]))

(def rows 25)
(def default-row-offset (- (.ceil js/Math (/ rows 2)))) ;; 0 is middle C, centered at 0
(def cols 16)

(defn log [x]
  (.log js/console (clj->js x))
  x)

(defn- cell [{:keys [on playing middle on-mouse]}]
  [:td {:class (str (when on "on ")
                    (when playing "playing ")
                    (when middle "middle"))
        :on-mouse-down on-mouse}])

(defn- piano-row [{:keys [row-note notes toggle-note playing-col]}]
  [:tr (for [col (range cols)
             :let [on (contains? notes col)]]
         ^{:key col}
         [cell {:on on
                :playing (= col playing-col)
                :middle (= 0 row-note)
                :on-mouse #(toggle-note col row-note)}])])

(defn piano-roll [{:keys [notes row-offset playing-col]}]
  (let [notes' @notes
        row-offset' @row-offset]
    [:table.piano-roll
     (for [row (range rows)
           :let [row-note (+ (- rows row) default-row-offset row-offset')]]
       ^{:key row}
       [piano-row {:row-note row-note
                   :notes (set (filter #(contains? (notes' %) row-note) (range cols)))
                   :playing-col playing-col
                   :toggle-note (fn [col row-note]
                                  (swap! notes
                                         (fn [notes]
                                           (update-in notes [col]
                                                                #((if (contains? (notes col) row-note) disj conj)
                                                                  (or % #{})
                                                                  row-note)))))}])]))
