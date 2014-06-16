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
    [boofcv.factory.feature.associate FactoryAssociation]
    [boofcv.factory.geo FactoryMultiView EnumEpipolar]
    [boofcv.abst.feature.detdesc DetectDescribePoint]
    [boofcv.abst.feature.associate ScoreAssociation]
    [boofcv.abst.feature.detect.interest ConfigFastHessian]
    [boofcv.abst.geo.fitting GenerateEpipolarMatrix DistanceFromModelResidual ModelManagerEpipolarMatrix]
    [boofcv.struct.feature SurfFeature TupleDesc AssociatedIndex]
    [boofcv.struct.image ImageFloat32 ImageSInt32]
    [boofcv.struct.geo AssociatedPair]
    [boofcv.alg.feature UtilFeature]
    [boofcv.alg.geo.f FundamentalResidualSampson]
    [boofcv.gui.feature AssociationPanel]
    [boofcv.gui.image ShowImages]
    [org.ddogleg.fitting.modelset.ransac Ransac]
    [java.util ArrayList List]
    ))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(def timg1 (h/load-file-image "/home/boechat/Dropbox/Photos/banknotes_samples/20reais_old.jpg"))
(def timg2 (h/load-file-image "/home/boechat/Dropbox/Photos/banknotes_samples/natural_100.jpg"))

(defn get-association-fn
  "Returns a function which calculates the associations between two images and
  returns a vector of AssociatedPair, accordingly with the feature description and
  scorer algorithms.
  Ex.:
  (let [assoc-fn (get-association-fn fa sc)]
    (assoc-fn img1 img2))"
  [^DetectDescribePoint feat-alg scorer]
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
        associate (FactoryAssociation/greedy scorer 1 true)]
    (fn [img-src img-dst]
      ;; Returns a vector of AssociatedPair.
      (let [gray-src (p/to-gray img-src)
            gray-dst (p/to-gray img-dst)
            ;; Interested points of the first image.
            [positions-src descriptions-src] (get-interested-pt (:mat gray-src))
            ;; Interested points of the desired image.
            [positions-dst descriptions-dst] (get-interested-pt (:mat gray-dst))]
        ;; Associating the features between the two images.
        (.setSource associate descriptions-src)
        (.setDestination associate descriptions-dst)
        (.associate associate)
        ;; Creation of a vector of associated pairs.
        (let [matches (.getMatches associate)]
          (mapv #(let [^AssociatedIndex a-idx (.get matches %)]
                   (AssociatedPair. (.get ^ArrayList positions-src (.src a-idx))
                                    (.get ^ArrayList positions-dst (.dst a-idx))))
                (range (.size matches))))))))

(defn inlier-pairs
  [pairs-list img1 img2]
  (let [;; First estimation of the fundamental matrix.
        estimation (-> (FactoryMultiView/computeFundamental_1 
                         EnumEpipolar/FUNDAMENTAL_7_LINEAR
                         20)
                      (GenerateEpipolarMatrix.))
        ;; Using ransac for a robust estimation of the matrix.
        ransac-model (Ransac. 
                       1234 ; random seed.
                       (ModelManagerEpipolarMatrix.) ; managerF
                       estimation ; first model.
                       ;; Error metric.
                       (DistanceFromModelResidual. (FundamentalResidualSampson.))
                       1000 ; max iterations.
                       ;; Threshold fit, how close of a fit a point needs to be.
                       0.1)]
    ;; Estimates the fundamental matrix while removing the outliers.
    (assert (.process ransac-model (ArrayList. ^List pairs-list)))
    (.getMatchSet ransac-model)))

(defn get-matches
  [img-src img-dst]
  (let [ddp (FactoryDetectDescribe/surfStable 
              (ConfigFastHessian. 1 2 200 1 9 4 4)
              nil
              nil 
              ImageSInt32)
        scorer (FactoryAssociation/scoreEuclidean SurfFeature true)
        assoc-fn (get-association-fn ddp scorer)
        ;; Visualization function to debug.
        debug (fn [pairs]
                (let [panel (AssociationPanel. 20)]
                  (.setAssociation panel pairs)
                  (.setImages panel
                              (h/to-buffered-image img-src)
                              (h/to-buffered-image img-dst))
                  (ShowImages/showWindow panel "Associated features"))
                pairs)]
    (-> (assoc-fn img-src img-dst)
        debug
        (inlier-pairs img-src img-dst)
        debug)
    )
  )


