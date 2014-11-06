(ns loopitude.synth-page
  (:require [reagent.core :as reagent :refer [atom]]
            [loopitude.synth :as synth]
            [loopitude.settings :as settings]
            [loopitude.piano-roll :as piano-roll :refer [piano-roll]]))

(def default-settings {:vol 0.125})

(defn synth-page []
  (let [started (reagent/atom false)
        settings (reagent/atom default-settings)
        row-offset (atom "0")
        loop-obj (atom nil)]
    (fn [{:keys [key hidden notes playing note-no tempo]}]
      (when (and @playing (not @started))
        (reset! started true)
        (reset! loop-obj (synth/loop! @notes 0 @tempo piano-roll/cols @settings)))
      (when (and (not @playing) @started)
        (reset! started false)
        ((:stop @loop-obj)))
      (when (not hidden)
        [:div
         [piano-roll {:hidden hidden
                      :notes notes
                      :row-offset (int @row-offset) 
                      :playing-col (if @playing @note-no nil)}]
         [:div
          "Note shift (octaves)"
          [:select {:on-change #(reset! row-offset (-> % .-target .-value))
                    :value @row-offset}
           [:option {:value "24"} "+2"]
           [:option {:value "12"} "+1"]
           [:option {:value "0"} "0"]
           [:option {:value "-12"} "-1"]
           [:option {:value "-24"} "-2"]]]
         [settings/volume settings]
         [settings/adsr settings]
         ]))))
