(ns loopitude.synth)

(def context (js/AudioContext.))

(defn freq [note]
  (* 261.626 ; middle c
     (.pow js/Math
           (.pow js/Math 2 (/ 1 12))
           note)))

(defn set-freq! [osc freq]
  (set! (.-value (.-frequency osc)) freq))

(defn play-note [osc freq start duration]
  (set! (.-value (.-frequency osc)) freq)
  (.start osc start)
  (.stop osc (+ start duration)))

(defn automate! [param coords]
  (.setValueAtTime param (ffirst coords) (second (first coords)))
  (doseq [[v t] (rest coords)]
    (.linearRampToValueAtTime param v t)))

(defn adsr!
  "a, d, r: seconds, s: 0 to 1"
  [param a d s r start duration]
  (automate! param (map (fn [[v t]] [v (+ t start)])
                        [[0 0]
                         [1 a]
                         [s (+ d a)]
                         [s duration]
                         [0 (+ r duration)]])))

(defn adsr-gain [a d s r start duration]
  (let [gain (.createGain context)]
    (adsr! (.-gain gain) a d s r start duration)
    gain))

(defn biquad [type' freq q gain]
  (let [bq (.createBiquadFilter context)]
    (set! (.-type bq) (str type'))
    (set! (.-value (.-frequency bq)) freq)
    (set! (.-value (.-Q bq)) q)
    (set! (.-value (.-gain bq)) gain)
    bq))

(defn chain-nodes! [& nodes]
  (reduce #(do (.connect %1 %2) %2)
          nodes))

(def default-settings {:a 0.03, :d 0.1, :s 0.5, :r 0.3
                       :lpf1-f 500,
                       :lpf2-f 2000,
                       :vol 0.1})

;; returns last node so can be disconnected
(defn osc [note start duration in-settings]
  (let [settings (merge default-settings in-settings)
        osc (.createOscillator context)
        gain (adsr-gain (settings :a) (settings :d) (settings :s) (settings :r) start duration)
        lpf (biquad "lowpass" (settings :lpf1-f) 1 0)
        lpf2 (biquad "lowpass" (settings :lpf2-f) 10 10)
        vol (.createGain context)]
    (set! (.-type osc) "triangle")
    (set! (.-value (.-gain vol)) (settings :vol))
    (chain-nodes! osc gain lpf lpf2 vol)
    (.connect vol (.-destination context))
    (play-note osc (freq note) start (+ duration (settings :r)))
    {:osc osc, :gain gain, :lpf lpf, :lpf2 lpf2, :vol vol}))

(defn quarter-time [tempo]
  (/ 60 tempo))

(defn audio-time []
  (.-currentTime context))

;; nasty and with lots of mutation. screw w3c.
(defn loop! [notes pitch-offset tempo length settings]
  (let [beat-time (quarter-time tempo)
        loop-time (* beat-time length)
        start-time (+ (.-currentTime context) 0.05)
        timeoutID (atom nil)
        oscs (atom [])
        schedule-more! (fn schedule-more! [loops-scheduled start-time settings notes]
                         (let [new-start-time (+ start-time (* loops-scheduled loop-time))]
                           (do (reset! oscs
                                       (concat @oscs
                                               (doall (for [[time pitches] notes
                                                            pitch pitches]
                                                        (osc (+ pitch pitch-offset)
                                                             (+ new-start-time
                                                                (* beat-time time))
                                                             beat-time
                                                             settings)))))
                               (reset! timeoutID (.setTimeout js/window
                                                              #(schedule-more! (inc loops-scheduled) start-time settings notes)
                                                              (* 1000 (- new-start-time (.-currentTime context))))))))
        stop! (fn []
                (doseq [osc @oscs]
                  (.disconnect (:vol osc)))
                (.clearTimeout js/window @timeoutID))
        update! (fn [settings]
                  (stop!)
                  (let [current-time (.-currentTime context)
                        offset (mod (- current-time start-time) loop-time)]
                    (schedule-more! 0 (- current-time offset) settings notes)))]
    (schedule-more! 0 start-time settings notes)
    {:stop! stop!
     :update! update!}))

