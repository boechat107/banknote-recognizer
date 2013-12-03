(ns banknote-processing.core
  (:require 
    [eye-boof
     [core :as c]])
  (:import 
    [boofcv.factory.feature.detdesc FactoryDetectDescribe]
    [boofcv.abst.feature.detdesc DetectDescribePoint]
    [boofcv.factory.feature.associate FactoryAssociation]
    [boofcv.abst.feature.associate ScoreAssociation]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
