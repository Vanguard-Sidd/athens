(ns athens.electron.boot
  (:require
    [electron.ipc-main :as ipc-main]
    [electron.web-contents :as web-contents]
    [clojure.string :as str]))

;; -----------------------------------------------------------------
;; IPC handlers for Find‑in‑Page functionality.
;; -----------------------------------------------------------------
(defn ^:private safe-options
  "Whitelist the options that are allowed to be passed to Electron's findInPage.
   Returns a JS object suitable for the native API."
  [{:keys [forward findNext matchCase wordStart]}]
  (clj->js {:forward   (boolean forward)
            :findNext  (boolean findNext)
            :matchCase (boolean matchCase)
            :wordStart (boolean wordStart)}))

(defn ^:private add-find-ipc-handlers!
  "Attaches IPC handlers to the given BrowserWindow's webContents.
   `wc` – the WebContents instance of the window."
  [wc]
  ;; Handler for initiating a find operation.
  (ipc-main/handle
    "find-in-page"
    (fn [_ {:keys [text options]}]
      (when (string? text)
        (let [opts (safe-options options)]
          (.findInPage wc text opts))))
    )
  ;; Handler for stopping/clearing a find operation.
  (ipc-main/handle
    "stop-find-in-page"
    (fn [_ {:keys [action]}]
      (let [act (or action "clearSelection")]
        (.stopFindInPage wc act))))
  ;; Forward native `found-in-page` events back to the renderer.
  (.on wc "found-in-page"
       (fn [_ result]
         (let [payload (js->clj result :keywordize-keys true)]
           (.send wc "found-in-page" payload))))
  wc)

;; -----------------------------------------------------------------
;; Public entry point – call this once the BrowserWindow is ready.
;; -----------------------------------------------------------------
(defn init-find-ipc!
  "Initialises Find‑in‑Page IPC for the supplied BrowserWindow.
   Should be invoked after the window has been created.
   Returns the window for convenience."
  [win]
  (let [wc (.webContents win)]
    (add-find-ipc-handlers! wc)
    win))
