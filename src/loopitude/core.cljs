(ns loopitude.core
  (:require [reagent.core :as reagent]
            [loopitude.piano-roll :refer [piano-roll]]
            [loopitude.synth :as synth]))

(def notes (reagent/atom {}))

(defn player []
  (let [playing (reagent/atom false)
        note-no (reagent/atom 0)
        tempo (reagent/atom 100)
        play-click (fn []
                     (reset! playing true)
                     (synth/play @notes 0 @tempo))]
    (fn []
      [:div
       [piano-roll {:notes notes, :row-offset 0, :playing-col (if @playing @note-no nil)}]
       [:button {:on-click play-click}
        "Play"]
       [:input {:type "range"
                :min "50"
                :max "500"
                :step "10"
                :on-change #(reset! tempo (-> % .-target .-value))}]])))

(reagent/render-component [player]
                          (.-body js/document))
