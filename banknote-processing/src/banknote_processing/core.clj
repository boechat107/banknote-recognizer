(ns banknote-processing.core
  (:require 
    [eye-boof
     [core :as c]
     [helpers :as h]
     [visualize :as v]
     [processing :as p]
     ])
  (:import 
    [boofcv.factory.feature.detdesc FactoryDetectDescribe]
    [boofcv.abst.feature.detdesc DetectDescribePoint]
    [boofcv.factory.feature.associate FactoryAssociation]
    [boofcv.abst.feature.associate ScoreAssociation]
    [boofcv.abst.feature.detect.interest ConfigFastHessian]
    [boofcv.struct.feature SurfFeature]
    [boofcv.struct.image ImageFloat32 ImageSInt32]
    [boofcv.alg.feature UtilFeature]
    [java.util ArrayList]
    
    [boofcv.gui.feature AssociationPanel]
    [boofcv.gui.image ShowImages]
    ))

(defn get-associations
  [img1 img2 ^DetectDescribePoint feat-alg scorer]
  (let [;; Function to detect the interested points of an image and returns their
        ;; locations and descriptions.
        get-interested-pt (fn [img]
                            (let [pos-array (ArrayList.)
                                  desc-queue (UtilFeature/createQueue feat-alg 100)]
                              ;; Detection of the features (side-effect).
                              (.detect feat-alg img)
                              (dotimes [i (.getNumberOfFeatures feat-alg)]
                                (.add pos-array (.copy (.getLocation feat-alg i)))
                                (.setTo (.grow desc-queue) 
                                        (.getDescription feat-alg i)))
                              [pos-array desc-queue]))
        ;; Interested points of the first image.
        [positions-src descriptions-src] (get-interested-pt (:mat img1))
        ;; Interested points of the desired image.
        [positions-dest descriptions-dest] (get-interested-pt (:mat img2))
        ;; The association method.
        associate (FactoryAssociation/greedy scorer 1 true)
        ]
    ;; Associating the features between the two images.
    (.setSource associate descriptions-src)
    (.setDestination associate descriptions-dest)
    (.associate associate)
    ;; Visualization for debug.
    (let [panel (AssociationPanel. 20)]
      (.setAssociation panel positions-src positions-dest (.getMatches associate))
      (.setImages panel (h/to-buffered-image img1) (h/to-buffered-image img2))
      (ShowImages/showWindow panel "Associated features"))
    )
  )

(defn get-matches
  []
  (let [ddp (FactoryDetectDescribe/surfStable 
              (ConfigFastHessian. 1 2 200 1 9 4 4)
              nil
              nil 
              ImageSInt32)
        scorer (FactoryAssociation/scoreEuclidean SurfFeature true)
        ]
    (get-associations (p/to-gray timg1) (p/to-gray timg2) ddp scorer)
    )
  )

(def timg1 (h/load-file-image "/home/andre/Dropbox/Photos/banknotes_samples/20reais_old.jpg"))
(def timg2 (h/load-file-image "/home/andre/Dropbox/Photos/banknotes_samples/natural_100.jpg"))
