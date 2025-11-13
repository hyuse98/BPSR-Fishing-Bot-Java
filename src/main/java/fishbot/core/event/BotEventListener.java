package fishbot.core.event;

import org.bytedeco.opencv.opencv_core.Mat;

/**
 * PADRÃO OBSERVER: Este é o nosso "Contrato".
 * Qualquer classe que quiser receber atualizações do bot (como a UI)
 * deve implementar esta interface. O Bot não faz ideia de quem está ouvindo.
 */
public interface BotEventListener {
    void onLogMessage(String message);
    void onStateChanged(String stateName);
    void onFishCaught(int totalCaught);

    default void onDetectionUpdate(String templateName, double score, boolean matched) {}
    default void onImageProcessed(Mat image) {}
}