package de.creditreform.crefoteam.cte.tesun.gui.jvm;

public interface JvmJobSwingWorkerListener {
    void notifyForProgress(int progressValue);

    void notifyForInfo(String strInfo);

    void notifyFinished();
}
