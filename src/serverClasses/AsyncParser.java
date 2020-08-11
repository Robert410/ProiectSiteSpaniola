package serverClasses;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class AsyncParser {
    private static int maxBuffer = 1048576;

    AsyncParser(String pathString)  {
        isFinished = false;
        Path path = Paths.get(pathString);
        System.out.println(path.toAbsolutePath().toString());
        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(maxBuffer);

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    attachment.flip();
                    byte[] data = new byte[attachment.limit()];
                    attachment.get(data);
                    attachment.clear();
                    readData = new String(data);
                    isFinished = true;
                }
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) { }
            });
        }   catch (IOException e) {}
    }

    public boolean isFinished;
    public String readData;
}
