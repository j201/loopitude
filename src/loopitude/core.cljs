(ns loopitude.core
  (:require [reagent.core :as reagent]
            [loopitude.piano-roll :refer [piano-roll]]
            [loopitude.synth :as synth]))

(def notes (reagent/atom {}))

(defn player []
  (let [playing (reagent/atom false)
        note-no (reagent/atom 0)
        play-click (fn []
                     (reset! playing true)
                     (synth/play @notes 0 200))]
    (fn []
      [:div
       [piano-roll {:notes notes, :row-offset 0, :playing-col (if @playing @note-no nil)}]
       [:button {:on-click play-click}
        "Play"]])))

(reagent/render-component [player]
                          (.-body js/document))
