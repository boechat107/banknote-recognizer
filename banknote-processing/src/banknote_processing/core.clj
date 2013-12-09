(ns banknote-processing.core
  (:require 
    [eye-boof
     [core :as c]])
  (:import 
    [boofcv.factory.feature.detdesc FactoryDetectDescribe]
    [boofcv.abst.feature.detdesc DetectDescribePoint]
    [boofcv.factory.feature.associate FactoryAssociation]
    [boofcv.abst.feature.associate ScoreAssociation]
    [boofcv.abst.feature.detect.interest ConfigFastHessian]
    [boofcv.struct.feature SurfFeature]
    [boofcv.struct.image ImageFloat32]
    ))

(defn get-associations)

(defn get-matches
  [bimg1 bimg2]
  (let [ddp (FactoryDetectDescribe/surfStable 
              (ConfigFastHessian. 1 2 200 1 9 4 4)
              nil
              nil 
              ImageFloat32)
        scorer (FactoryAssociation/scoreEuclidean SurfFeature true)
        associate (FactoryAssociation/greedy scorer 1 true)
        ])
  )
