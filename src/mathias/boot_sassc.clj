(ns mathias.boot-sassc
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]
            [boot.core       :as core]
            [boot.file       :as file]
            [boot.util       :as util]))

(defn valid-style? [style]
  (some #{"nested" "compressed"} [style]))

(defn sass-filename->css-filename
  [filename]
  (string/replace filename #".s(c|a)ss$" ".css"))

(defn- sassc [input-file output-dir & {:keys [output-style
                                              line-numbers
                                              source-maps
                                              load-path]}]
  (let [output-file (->> input-file
                         .getName
                         sass-filename->css-filename
                         (io/file output-dir))]
    (apply util/dosh (concat ["sassc"]
                             (when (and output-style
                                        (valid-style? output-style))
                               ["-t" output-style])
                             (when line-numbers ["-l"])
                             (when source-maps ["-m"])
                             (when load-path ["-I" load-path])
                             [(file/path input-file) (file/path output-file)]))))

(defn by-name-re
  [res files & [negate?]]
  ((core/file-filter #(fn [f] (re-find % (.getName f)))) res files negate?))

(defn not-by-name-re
  [res files]
  (by-name-re res files true))

(defn leading-underscore
  [files]
  (by-name-re [#"^_"] files))

(defn not-leading-underscore
  [files]
  (not-by-name-re [#"^_"] files))

(core/deftask sass
  "Compile SCSS to CSS files.  Requires that the sassc executable (3.1 or better) is available on the $PATH.

   Rules for compilation:

   - If --sass-file is provided, then it is compiled whenever it is changed or whenever
     any SASS partials in the project are changed or removed
   - If --sass-file is not provided:
     - If no SASS partials have changed or been removed, only the changed SASS files are compiled.
     - If any SASS partials in the project have changed or been removed, all SASS files are compiled
       regardless of whether they themselves have changed."
  [f sass-file FILE      str  "Input file. If not present, all .sass & .scss files will be compiled."
   o output-dir PATH     str  "Output CSS directory path, relative to target directory."
   t output-style TYPE   str  "Output style. Can be: nested, compressed."
   l line-numbers        bool "Emit comments showing original line numbers."
   g source-maps         bool "Emit source map."
   p load-path           str  "Load path for libsass. Use : for separate paths."]
  (let [tmp-dir      (core/temp-dir!)
        output-dir   (if output-dir (io/file tmp-dir output-dir) tmp-dir)
        last-fileset (atom nil)]
    (core/with-pre-wrap fileset
      (let [last-fileset-val @last-fileset
            diff             (->> fileset
                                  (core/fileset-diff last-fileset-val)
                                  core/input-files
                                  (core/by-ext [".sass" ".scss"]))
            removed          (->> fileset
                                  (core/fileset-removed last-fileset-val)
                                  core/input-files
                                  (core/by-ext [".sass" ".scss"]))
            added            (->> fileset
                                  (core/fileset-added last-fileset-val)
                                  core/input-files
                                  (core/by-ext [".sass" ".scss"]))
            sass-files       (if (or (seq (leading-underscore diff))
                                     (seq removed)
                                     (seq added))
                               (do
                                 (core/empty-dir! tmp-dir)
                                 (cond->> fileset
                                          true      core/input-files
                                          true      (core/by-ext [".sass" ".scss"])
                                          true      not-leading-underscore
                                          sass-file (core/by-name [sass-file])))
                               (cond->> diff
                                        true      not-leading-underscore
                                        sass-file (core/by-name [sass-file])))]
        (reset! last-fileset fileset)
        (util/dbug "Compiling %d changed SASS files... .\n" (count sass-files))
        (.mkdirs output-dir)
        (doseq [file sass-files]
          (util/info "Compiling %s\n" (core/tmppath file))
          (sassc (core/tmpfile file)
                 output-dir
                 :output-style output-style
                 :line-numbers line-numbers
                 :source-maps  source-maps
                 :load-path    load-path))
        (util/dbug "...done.\n" (count sass-files))
        (-> fileset
            (core/add-resource tmp-dir)
            core/commit!)))))
