package fishbot.core.event;

import org.bytedeco.opencv.opencv_core.Mat;

public interface BotEventBus {

    public void register(BotEventListener listener);
    public void unregister(BotEventListener listener);
    public void publishLog(String message);
    public void publishStateChange(String stateName);
    public void publishFishCaught(int totalCaught);
    public void publishDetectionUpdate(String templateName, double score, boolean matched);
    public void publishImageProcessed(Mat image);
}
