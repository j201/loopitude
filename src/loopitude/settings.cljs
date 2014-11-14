(ns loopitude.settings
  (:refer-clojure :exclude [assoc!]))

(defn- e-val [e]
  (.-value (.-target e)))

(defn- logish [x]
  (* x x x))

; for calculating control values
(defn- delogish [x]
  (.pow js/Math x (/ 1 3)))

(defn- bangify [f]
  (fn [map-atom & args]
    (reset! map-atom
            (apply (partial f @map-atom) args))))

(def ^:private assoc! (bangify assoc))
(def ^:private assoc-in! (bangify assoc-in))

(defn- range1 "A range input from 0 to 1" [default on-change]
  [:input {:type "range", :min "0", :max "1", :step "0.01", :default-value (str default)
           :class "settings-range"
           :on-change #(on-change (js/parseFloat (e-val %)))}])

(defn volume [settings]
  [:div
   "Volume"
   [range1 0.5 #(assoc! settings :vol (logish %))]])

(defn adsr [settings]
  [:div
   "ADSR "
   [:span "A" [range1
               (delogish 0.03)
               #(assoc! settings :a (logish %))]]
   [:span "D" [range1
               (delogish 0.1)
               #(assoc! settings :d (logish %))]]
   [:span "S" [range1
               0.5
               #(assoc! settings :s %)]]
   [:span "R" [range1
               (delogish 0.3)
               #(assoc! settings :r (* 5 (logish %)))]]])

(defn filt [settings filt-kw name]
  [:div
   name
   [:span "Freq" [range1
                  0.5
                  #(assoc-in! settings [filt-kw :f] (* 20 (.pow js/Math 1000 %)))]]
   [:span "Q" [range1
               0.25
               #(assoc-in! settings [filt-kw :q] (dec (.pow js/Math 15 %)))]]
   [:span "Gain" [range1
                  0
                  #(assoc-in! settings [filt-kw :gain] (* 20 %))]]])
