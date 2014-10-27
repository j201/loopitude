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

(defn osc [note start duration]
  (let [osc (.createOscillator context)
        gain (adsr-gain 0.03 0.1 0.5 0.3 start duration)
        lpf (biquad "lowpass" 500 1 0)
        lpf2 (biquad "lowpass" 2000 10 10)
        vol (.createGain context)]
    (set! (.-type osc) "triangle")
    (set! (.-value (.-gain vol)) 0.1)
    (chain-nodes! osc gain lpf lpf2 vol)
    (.connect vol (.-destination context))
    (play-note osc (freq note) start (+ duration 0.3))))

(defn quarter-time [tempo]
  (/ 60 tempo))

(defn play [notes pitch-offset tempo]
  (let [beat-time (quarter-time tempo)]
    (doseq [[time pitches] notes
            pitch pitches]
      (osc (+ pitch pitch-offset)
           (+ (.-currentTime context)
              (* beat-time time))
           beat-time))))
