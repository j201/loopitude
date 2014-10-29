(ns loopitude.synth-page
  (:require [reagent.core :as reagent]
            [loopitude.synth :as synth]
            [loopitude.piano-roll :as piano-roll :refer [piano-roll]]))

(defn synth-page [{:keys [key hidden notes playing note-no tempo]}]
  (.log js/console hidden)
  (let [started (reagent/atom false)
        loop-obj (atom nil)]
    (fn []
      (do
        (when (and @playing (not @started))
          (reset! started true)
          (reset! loop-obj (synth/loop! @notes 0 @tempo piano-roll/cols)))
        (when (and (not @playing) @started)
          (reset! started false)
          ((:stop @loop-obj)))
        [piano-roll {:hidden hidden
                     :notes notes
                     :row-offset 0
                     :playing-col (if @playing @note-no nil)}]))))
