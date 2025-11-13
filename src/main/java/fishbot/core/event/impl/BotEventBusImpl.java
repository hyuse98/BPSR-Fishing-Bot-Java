package fishbot.core.event.impl;

import fishbot.core.event.BotEventBus;
import fishbot.core.event.BotEventListener;
import org.bytedeco.opencv.opencv_core.Mat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotEventBusImpl implements BotEventBus {

    private final List<BotEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(BotEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregister(BotEventListener listener) {
        listeners.remove(listener);
    }

    public void publishLog(String message) {
        for (BotEventListener listener : listeners) {
            listener.onLogMessage(message);
        }
    }

    public void publishStateChange(String stateName) {
        for (BotEventListener listener : listeners) {
            listener.onStateChanged(stateName);
        }
    }

    public void publishFishCaught(int totalCaught) {
        for (BotEventListener listener : listeners) {
            listener.onFishCaught(totalCaught);
        }
    }

    public void publishDetectionUpdate(String templateName, double score, boolean matched) {
        for (BotEventListener listener : listeners) {
            listener.onDetectionUpdate(templateName, score, matched);
        }
    }

    public void publishImageProcessed(Mat image) {
        for (BotEventListener listener : listeners) {
            listener.onImageProcessed(image);
        }
    }
}