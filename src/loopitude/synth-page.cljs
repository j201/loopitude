(ns loopitude.synth-page
  (:require [reagent.core :as reagent :refer [atom]]
            [loopitude.synth :as synth]
            [loopitude.settings :as settings]
            [loopitude.piano-roll :as piano-roll :refer [piano-roll]]))

(def default-settings {:vol 0.125})

;; TODO: move to utils?
(defn throttle [ms f]
  (let [waiting (atom false)
        called-while-waiting (atom false)
        args (atom [])]
    (fn [& args']
      (reset! args args')
      (if @waiting
        (reset! called-while-waiting true)
        (do (apply f @args)
            (reset! waiting true)
            (reset! called-while-waiting false)
            (js/setTimeout (fn []
                             (when @called-while-waiting
                               (apply f @args))
                             (reset! waiting false))
                           ms))))))

(defn synth-page []
  (let [started (reagent/atom false)
        settings (reagent/atom default-settings)
        row-offset (atom "0")
        loop-obj (atom nil)
        on-note-change (fn [notes]
                         (when @loop-obj
                           ((:update! @loop-obj) {:notes notes})))]
    (add-watch settings :watch-change (throttle 200 (fn [key ref old new]
                                                      (when @loop-obj
                                                        ((:update! @loop-obj) {:settings new})))))
    (fn [{:keys [key hidden notes playing note-no tempo]}]
      (when (and @playing (not @started))
        (reset! started true)
        (reset! loop-obj (synth/loop! @notes 0 @tempo piano-roll/cols @settings)))
      (when (and (not @playing) @started)
        (reset! started false)
        ((:stop! @loop-obj))
        (reset! loop-obj nil))
      (when (not hidden)
        [:div
         [piano-roll {:hidden hidden
                      :notes notes
                      :on-note-change on-note-change
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
         [settings/filt settings :filt1]
         [settings/filt settings :filt2]
         ]))))
