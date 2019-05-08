(ns kmeans-clj.core
  "A simple k-means clustering implementation."
  (:require [incanter.core :as i]
            [kmeans-clj.util :refer :all]
            [clojure.core.async :refer [chan to-chan <!!]]
            [clojure.set :refer [union difference]]))

(defn- cluster-vector
  [fe cluster]
  (i/div (reduce i/plus (map fe cluster)) (count cluster)))

(defn k-means
  "Performs k-means clustering on the elements into k clusters until
   convergence or the maximum number of iterations is reached. Function
   distance is a pairwise distance function, and fe a feature extractor
   that returns a vector for a given element."
  [elements distance fe k max-iter]
  (loop
    [clusters-1 nil
     clusters (map (partial cluster-vector fe) (partition-all k (shuffle elements)))
     cluster-vals []
     iter 0]
    (if
      (or (= clusters clusters-1) (= max-iter iter)) cluster-vals
      (let [input (to-chan elements)
            output (chan)
            results (sink output)
            _ (<!! (parallel
                     (.availableProcessors (Runtime/getRuntime))
                     (fn [e]
                       (let [evec (fe e)]
                         [(argmin
                            #(distance evec %)
                            clusters)
                          e]))
                     input
                     output))
            new-cluster-vals (set (->>
                                    @results
                                    (group-by first)
                                    (fmap #(map second %))
                                    vals))]
        (recur
          clusters
          ; Ensure we do not lose any old clusters which did not receive a match
          (union (set (map (partial cluster-vector fe) new-cluster-vals))
                 (difference (set clusters) (set (map first @results))))
          new-cluster-vals
          (inc iter))))))
