package de.creditreform.crefoteam.cte.tesun.logsearch;



public interface IWorkerListener
{
  public enum LOG_LEVEL { DEBUG, INFO, WARN, ERROR }
  public enum TASK_STATE { IDLE, RUNNING, CANCELLED, ABORTED, PAUSED, DONE }

  public void updateProgress( Object dataObject, int progressStep );

  public void updateData( Object dataObject  );

  public void updateTaskState( TASK_STATE taskState );

  public boolean isCanceled();

}
