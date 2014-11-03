(ns loopitude.core
  (:require [reagent.core :as reagent]
            [loopitude.piano-roll :as piano-roll :refer [piano-roll]]
            [loopitude.synth-page :refer [synth-page]]
            [loopitude.synth :as synth]))

(def tempo (reagent/atom 200))

(def synth-pages 4)
(def notes (map #(%) (repeat synth-pages #(reagent/atom {}))))
(def shown-piano-roll (reagent/atom 0))

(defn player []
  (let [playing (reagent/atom false)
        note-no (reagent/atom 0)]
    (fn []
      (let [shown-piano-roll' @shown-piano-roll]
        [:div
         (for [i (range synth-pages)]
           ^{:key i}
           [:button
            {:on-click #(reset! shown-piano-roll i)}
            (str "Synth " i)])
         (for [i (range synth-pages)]
           ^{:key i}
           [:div
            [synth-page {:hidden (not= i shown-piano-roll') 
                        :notes (nth notes i)
                        :playing playing
                        :note-no note-no
                        :tempo tempo}]])
         [:button
          {:on-click #(reset! playing (not @playing))}
          (if @playing "Stop" "Play")]
         [:input {:type "range" :min "100" :max "500" :step "10"
                  :on-change #(reset! tempo (.-value (.-target %)))}]
         [:div
          [:button
           {:on-click #(reset! (nth notes @shown-piano-roll) {})}
           "Clear"]
          [:button
           {:on-click #(doseq [notes-map notes]
                         (reset! notes-map {}))}
           "Clear All"]]]))))

(reagent/render-component [player]
                          (.-body js/document))
