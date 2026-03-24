package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.util.List;

public interface SearchForWordWorkerListener {
   void processChunks(List<SearchForWordWorkerChunk> chunks);
}
