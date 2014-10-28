(ns loopitude.core
  (:require [reagent.core :as reagent]
            [loopitude.piano-roll :as piano-roll :refer [piano-roll]]
            [loopitude.synth :as synth]))

(def notes (reagent/atom {}))
(def tempo (reagent/atom 200))

(defn player []
  (let [playing (reagent/atom false)
        note-no (reagent/atom 0)
        loop-obj (atom nil)
        play-click (fn []
                     (if @playing
                       (do (reset! playing false)
                           ((:stop @loop-obj)))
                       (do (reset! playing true)
                           (reset! loop-obj (synth/loop! @notes 0 @tempo piano-roll/cols)))))]
    (fn []
      [:div
       [piano-roll {:notes notes, :row-offset 0, :playing-col (if @playing @note-no nil)}]
       [:button {:on-click play-click} (if @playing "Stop" "Play")]
       [:input {:type "range" :min "100" :max "500" :step "10"
                :on-change #(reset! tempo (.-value (.-target %)))}]])))

(reagent/render-component [player]
                          (.-body js/document))
