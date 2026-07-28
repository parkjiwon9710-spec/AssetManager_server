package server;

public class SoundSettingLoadRequest {
    public String type = "SOUND_SETTING_LOAD_REQUEST";
    public int userId;

    public SoundSettingLoadRequest(int userId) {
        this.userId = userId;
    }
}