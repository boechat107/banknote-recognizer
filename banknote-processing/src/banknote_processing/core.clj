(ns banknote-processing.core
  (:require 
    [eye-boof
     [core :as c]
     [helpers :as h]
     [visualize :as v]
     [processing :as p :only [to-gray]]
     ])
  (:import 
    [boofcv.factory.feature.detdesc FactoryDetectDescribe]
    [boofcv.abst.feature.detdesc DetectDescribePoint]
    [boofcv.factory.feature.associate FactoryAssociation]
    [boofcv.abst.feature.associate ScoreAssociation]
    [boofcv.abst.feature.detect.interest ConfigFastHessian]
    [boofcv.struct.feature SurfFeature TupleDesc]
    [boofcv.struct.image ImageFloat32 ImageSInt32]
    [boofcv.alg.feature UtilFeature]
    [java.util ArrayList]
    [boofcv.gui.feature AssociationPanel]
    [boofcv.gui.image ShowImages]
    ))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn get-association-fn
  "Returns a function which calculates the associations between two images
  accordingly with the feature description and scorer algorithms.
  If the last argument is :debug, a window is showed to visulize the associations
  between the images.
  Ex.:
  (let [assoc-fn (get-association-fn fa sc)]
    (assoc-fn img1 img2))
  => {:association associate 
      :src-points positions-src
      :dst-points positions-dest}"
  [^DetectDescribePoint feat-alg scorer & opts]
  (let [;; Function to detect the interested points of an image and returns their
        ;; locations and descriptions.
        get-interested-pt (fn [img]
                            (let [pos-array (ArrayList.)
                                  desc-queue (UtilFeature/createQueue feat-alg 100)]
                              ;; Detection of the features (side-effect).
                              (.detect feat-alg img)
                              (dotimes [i (.getNumberOfFeatures feat-alg)]
                                (.add pos-array (.copy (.getLocation feat-alg i)))
                                (.setTo ^TupleDesc (.grow desc-queue) 
                                        (.getDescription feat-alg i)))
                              [pos-array desc-queue]))
        ;; The association method.
        associate (FactoryAssociation/greedy scorer 1 true) 
        debug (some #(= :debug %) opts)]
    (fn [img-src img-dst]
      ;; Returns the AssociateDescription object and the source/destination points 
      ;; positions.
      (let [gray-src (p/to-gray img-src)
            gray-dst (p/to-gray img-dst)
            ;; Interested points of the first image.
            [positions-src descriptions-src] (get-interested-pt (:mat gray-src))
            ;; Interested points of the desired image.
            [positions-dest descriptions-dest] (get-interested-pt (:mat gray-dst))]
        ;; Associating the features between the two images.
        (.setSource associate descriptions-src)
        (.setDestination associate descriptions-dest)
        (.associate associate)
        ;; Visualization for debug.
        (when debug
          (let [panel (AssociationPanel. 20)]
            (.setAssociation panel positions-src positions-dest (.getMatches associate))
            (.setImages panel (h/to-buffered-image gray-src) (h/to-buffered-image gray-dst))
            (ShowImages/showWindow panel "Associated features")))
        {:association associate 
         :src-points positions-src
         :dst-points positions-dest}))))

(def timg1 (h/load-file-image "/home/andre/Dropbox/Photos/banknotes_samples/20reais_old.jpg"))
(def timg2 (h/load-file-image "/home/andre/Dropbox/Photos/banknotes_samples/natural_100.jpg"))

(defn get-matches
  [img-src img-dst]
  (let [ddp (FactoryDetectDescribe/surfStable 
              (ConfigFastHessian. 1 2 200 1 9 4 4)
              nil
              nil 
              ImageSInt32)
        scorer (FactoryAssociation/scoreEuclidean SurfFeature true)
        assoc-fn (get-association-fn ddp scorer :debug)]
    (assoc-fn img-src img-dst)
    )
  )

