(ns loopitude.core
  (:require [reagent.core :as reagent]
            [loopitude.piano-roll :refer [piano-roll]]))

(def notes (reagent/atom {}))

(reagent/render-component [piano-roll {:notes notes, :row-offset 0, :playing-col nil}]
                          (.-body js/document))
