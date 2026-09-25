(ns athens.electron.window
  (:require
    [electron.global-shortcut :as global-shortcut]
    [electron.ipc-main :as ipc-main]
    [re-frame.core :as rf]))

;; -----------------------------------------------------------------
;; Register the Find (Ctrl/Cmd+F) shortcut for a given BrowserWindow.
;; -----------------------------------------------------------------
(defn register-find-shortcut!
  "Registers CommandOrControl+F to open the custom Find dialog.
   `win` is the Electron BrowserWindow instance.
   The shortcut sends an IPC message to the renderer to open the UI.
   It also dispatches a re-frame event for any additional app state handling."
  [win]
  (let [shortcut (if (.-platform js/process) "CommandOrControl+F" "Ctrl+F")]
    (global-shortcut/register shortcut
      (fn []
        ;; Notify the renderer process to display the Find dialog.
        (.send (.webContents win) "open-find-dialog")
        ;; Optionally update app state via re-frame.
        (rf/dispatch [:find-dialog/open])))))

;; Ensure the shortcut is registered when the window is created.
;; This function should be called from the main process after the BrowserWindow is instantiated.
