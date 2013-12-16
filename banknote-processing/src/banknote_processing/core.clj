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
    [boofcv.abst.geo.fitting GenerateEpipolarMatrix]
    [boofcv.struct.feature SurfFeature TupleDesc AssociatedIndex]
    [boofcv.struct.image ImageFloat32 ImageSInt32]
    [boofcv.struct.geo AssociatedPair]
    [boofcv.alg.feature UtilFeature]
    [boofcv.gui.feature AssociationPanel]
    [boofcv.gui.image ShowImages]
    [java.util ArrayList]
    ))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn get-association-fn
  "Returns a function which calculates the associations between two images and
  returns a vector of AssociatedPair, accordingly with the feature description and
  scorer algorithms.
  If the last argument is :debug, a window is showed to visulize the associations
  between the images.
  Ex.:
  (let [assoc-fn (get-association-fn fa sc)]
    (assoc-fn img1 img2))"
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
        ;; Visualization for debug.
        (when debug
          (let [panel (AssociationPanel. 20)]
            (.setAssociation panel positions-src positions-dst (.getMatches associate))
            (.setImages panel (h/to-buffered-image gray-src) (h/to-buffered-image gray-dst))
            (ShowImages/showWindow panel "Associated features")))
        ;; Creation of a vector of associated pairs.
        (let [matches (.getMatches associate)]
          (mapv #(let [^AssociatedIndex a-idx (.get matches %)]
                   (AssociatedPair. (.get ^ArrayList positions-src (.src a-idx))
                                    (.get ^ArrayList positions-dst (.dst a-idx))))
                (range (.size matches))))))))

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

(defn fundamental-matrix
  [pairs-list]
  (let [estimator (-> (FactoryMultiView/computeFundamental_l 
                        EnumEpipolar/FUNDAMENTAL_7_LINEAR
                        20)
                      (GenerateEpipolarMatrix.))
        ])
  )
