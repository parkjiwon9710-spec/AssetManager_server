package server;

public class SoundSettingSaveResponse {
    public String type = "SOUND_SETTING_SAVE_RESPONSE";
    public boolean success;
    public String message;

    public SoundSettingSaveResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}