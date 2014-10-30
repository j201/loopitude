(ns loopitude.synth-page
  (:require [reagent.core :as reagent]
            [loopitude.synth :as synth]
            [loopitude.piano-roll :as piano-roll :refer [piano-roll]]))

(defn synth-page []
  (let [started (reagent/atom false)
        row-offset (atom 0)
        loop-obj (atom nil)]
    (fn [{:keys [key hidden notes playing note-no tempo]}]
      (do
        (when (and @playing (not @started))
          (reset! started true)
          (reset! loop-obj (synth/loop! @notes 0 @tempo piano-roll/cols)))
        (when (and (not @playing) @started)
          (reset! started false)
          ((:stop @loop-obj)))
        (when (not hidden)
          [:div
           [piano-roll {:hidden hidden
                        :notes notes
                        :row-offset row-offset 
                        :playing-col (if @playing @note-no nil)}]
           [:div
            "Note shift (octaves)"
            [:select {:on-change #(reset! row-offset (-> % .-target .-value int))}
             [:option {:value 24} "+2"]
             [:option {:value 12} "+1"]
             [:option {:value 0, :selected "selected"} "0"]
             [:option {:value -12} "-1"]
             [:option {:value -24} "-2"]]]])))))
