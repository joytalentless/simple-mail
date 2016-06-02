package sockslib.server.io;

public interface Pipe {

  /**
   * Start the pipe, the pipe will work with a new thread.
   */
  boolean start();

  /**
   * Stop the pipe, the pipe will stop transferring data.
   */
  void stop();

  void setBufferSize(int bufferSize);

  boolean isRunning();

  void addPipeListener(PipeListener pipeListener);

  void removePipeListener(PipeListener pipeListener);

  void setName(String name);

}
